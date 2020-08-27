package net.fabricmc.loader.discovery;

import ai.arcblroth.mixon.MixonModLoader;
import com.google.gson.*;

import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.game.GameProvider;
import net.fabricmc.loader.launch.common.FabricLauncherBase;
import net.fabricmc.loader.metadata.LoaderModMetadata;
import net.fabricmc.loader.metadata.ModMetadataParser;
import net.fabricmc.loader.metadata.NestedJarEntry;
import net.fabricmc.loader.util.FileSystemUtil;
import net.fabricmc.loader.util.UrlConversionException;
import net.fabricmc.loader.util.UrlUtil;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MixonModResolver extends ModResolver {

    private static final Pattern MOD_ID_PATTERN = Pattern.compile("[a-z][a-z0-9-_]{1,63}");

    @Override
    public Map<String, ModCandidate> resolve(FabricLoader loader) throws ModResolutionException {
        ConcurrentMap<String, ModCandidateSet> candidatesById = new ConcurrentHashMap<>();

        long time1 = System.currentTimeMillis();

        Queue<UrlProcessAction> allActions = new ConcurrentLinkedQueue<>();
        ForkJoinPool pool = new ForkJoinPool(Math.max(1, Runtime.getRuntime().availableProcessors() - 1));
        for (ModCandidateFinder f : getCandidateFinders()) {
            f.findCandidates(loader, (u) -> {
                UrlProcessAction action = new UrlProcessAction(loader, candidatesById, u, 0);
                allActions.add(action);
                pool.execute(action);
            });
        }

        // add builtin mods
        for (GameProvider.BuiltinMod mod : loader.getGameProvider().getBuiltinMods()) {
            candidatesById.computeIfAbsent(mod.metadata.getId(), ModCandidateSet::new).add(new ModCandidate(new MixonBuiltinMetadataWrapper(mod.metadata), mod.url, 0));
        }

        boolean tookTooLong = false;
        Throwable exception = null;
        try {
            pool.shutdown();
            pool.awaitTermination(30, TimeUnit.SECONDS);
            for (UrlProcessAction action : allActions) {
                if (!action.isDone()) {
                    tookTooLong = true;
                } else {
                    Throwable t = action.getException();
                    if (t != null) {
                        if (exception == null) {
                            exception = t;
                        } else {
                            exception.addSuppressed(t);
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            throw new ModResolutionException("Mod resolution took too long!", e);
        }
        if (tookTooLong) {
            throw new ModResolutionException("Mod resolution took too long!");
        }
        if (exception != null) {
            throw new ModResolutionException("Mod resolution failed!", exception);
        }

        long time2 = System.currentTimeMillis();
        Map<String, ModCandidate> result = findCompatibleSet(loader.getLogger(), candidatesById);

        long time3 = System.currentTimeMillis();
        loader.getLogger().debug("Mod resolution detection time: " + (time2 - time1) + "ms");
        loader.getLogger().debug("Mod resolution time: " + (time3 - time2) + "ms");

        for (ModCandidate candidate : result.values()) {
            if (candidate.getInfo().getSchemaVersion() < ModMetadataParser.LATEST_VERSION) {
                loader.getLogger().warn("Mod ID " + candidate.getInfo().getId() + " uses outdated schema version: " + candidate.getInfo().getSchemaVersion() + " < " + ModMetadataParser.LATEST_VERSION);
            }

            candidate.getInfo().emitFormatWarnings(loader.getLogger());
        }

        return result;
    }

    // ----[ BEGIN MIXON STUFF ]----

    @SuppressWarnings("unchecked")
    private List<ModCandidateFinder> getCandidateFinders() throws ModResolutionException {
        try {
            Field candidateFinders = ModResolver.class.getDeclaredField("candidateFinders");
            candidateFinders.setAccessible(true);
            return (List<ModCandidateFinder>) candidateFinders.get(this);
        } catch (ReflectiveOperationException e) {
            throw new ModResolutionException(e);
        }
    }

    private Object getLauncherSyncObject() {
        try {
            Field launcherSyncObject = ModResolver.class.getDeclaredField("launcherSyncObject");
            launcherSyncObject.setAccessible(true);
            return launcherSyncObject.get(this);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<Path>> getInMemoryCache() {
        try {
            Field inMemoryCache = ModResolver.class.getDeclaredField("inMemoryCache");
            inMemoryCache.setAccessible(true);
            return (Map<String, List<Path>>) inMemoryCache.get(this);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private FileSystem getInMemoryFs() {
        try {
            Field inMemoryFs = ModResolver.class.getDeclaredField("inMemoryFs");
            inMemoryFs.setAccessible(true);
            return (FileSystem) inMemoryFs.get(this);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isModIdValid(String modId, List<String> errorList) {
        try {
            Method isModIdValid = ModResolver.class.getDeclaredMethod("isModIdValid", String.class, List.class);
            isModIdValid.setAccessible(true);
            return (boolean) isModIdValid.invoke(null, modId, errorList);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    // ----[ END MIXON STUFF ]----

    class UrlProcessAction extends RecursiveAction {
        private final FabricLoader loader;
        private final Map<String, ModCandidateSet> candidatesById;
        private final URL url;
        private final int depth;

        UrlProcessAction(FabricLoader loader, Map<String, ModCandidateSet> candidatesById, URL url, int depth) {
            this.loader = loader;
            this.candidatesById = candidatesById;
            this.url = url;
            this.depth = depth;
        }

        @Override
        protected void compute() {
            FileSystemUtil.FileSystemDelegate jarFs;
            Path path, modJson, rootDir;
            URL normalizedUrl;

            loader.getLogger().debug("Testing " + url);

            try {
                path = UrlUtil.asPath(url).normalize();
                // normalize URL (used as key for nested JAR lookup)
                normalizedUrl = UrlUtil.asUrl(path);
            } catch (UrlConversionException e) {
                throw new RuntimeException("Failed to convert URL " + url + "!", e);
            }

            if (Files.isDirectory(path)) {
                // Directory
                modJson = path.resolve("fabric.mod.json");
                rootDir = path;

                if (loader.isDevelopmentEnvironment() && !Files.exists(modJson)) {
                    loader.getLogger().warn("Adding directory " + path + " to mod classpath in development environment - workaround for Gradle splitting mods into two directories");
                    synchronized (getLauncherSyncObject()) {
                        FabricLauncherBase.getLauncher().propose(url);
                    }
                }
            } else {
                // JAR file
                try {
                    jarFs = FileSystemUtil.getJarFileSystem(path, false);
                    modJson = jarFs.get().getPath("fabric.mod.json");
                    rootDir = jarFs.get().getRootDirectories().iterator().next();
                } catch (IOException e) {
                    throw new RuntimeException("Failed to open mod JAR at " + path + "!");
                }
            }

            LoaderModMetadata[] info;

            try (InputStream stream = Files.newInputStream(modJson)) {
                info = ModMetadataParser.getMods(loader, stream);
            } catch (JsonParseException e) {
                throw new RuntimeException(String.format("Mod at \"%s\" has an invalid fabric.mod.json file!", path), e);
            } catch (NoSuchFileException e) {
                info = new LoaderModMetadata[0];
            } catch (IOException e) {
                throw new RuntimeException(String.format("Failed to open fabric.mod.json for mod at \"%s\"!", path), e);
            } catch (Throwable t) {
                throw new RuntimeException(String.format("Failed to parse mod metadata for mod at \"%s\"", path), t);
            }

            // ----[ BEGIN MIXON STUFF ]----
            if(info.length > 0) {
                info = MixonModLoader.transformModMetadata(info);
            }
            // ----[ END MIXON STUFF ]----

            for (LoaderModMetadata i : info) {
                ModCandidate candidate = new ModCandidate(i, normalizedUrl, depth);
                boolean added;

                if (candidate.getInfo().getId() == null || candidate.getInfo().getId().isEmpty()) {
                    throw new RuntimeException(String.format("Mod file `%s` has no id", candidate.getOriginUrl().getFile()));
                }

                if (!MOD_ID_PATTERN.matcher(candidate.getInfo().getId()).matches()) {
                    List<String> errorList = new ArrayList<>();
                    isModIdValid(candidate.getInfo().getId(), errorList);
                    StringBuilder fullError = new StringBuilder("Mod id `");
                    fullError.append(candidate.getInfo().getId()).append("` does not match the requirements because");

                    if (errorList.size() == 1) {
                        fullError.append(" it ").append(errorList.get(0));
                    } else {
                        fullError.append(":");
                        for (String error : errorList) {
                            fullError.append("\n  - It ").append(error);
                        }
                    }

                    throw new RuntimeException(fullError.toString());
                }

                added = candidatesById.computeIfAbsent(candidate.getInfo().getId(), ModCandidateSet::new).add(candidate);

                if (!added) {
                    loader.getLogger().debug(candidate.getOriginUrl() + " already present as " + candidate);
                } else {
                    loader.getLogger().debug("Adding " + candidate.getOriginUrl() + " as " + candidate);

                    List<Path> jarInJars = getInMemoryCache().computeIfAbsent(candidate.getOriginUrl().toString(), (u) -> {
                        loader.getLogger().debug("Searching for nested JARs in " + candidate);
                        Collection<NestedJarEntry> jars = candidate.getInfo().getJars();
                        List<Path> list = new ArrayList<>(jars.size());

                        jars.stream()
                                .map((j) -> rootDir.resolve(j.getFile().replace("/", rootDir.getFileSystem().getSeparator())))
                                .forEach((modPath) -> {
                                    if (!Files.isDirectory(modPath) && modPath.toString().endsWith(".jar")) {
                                        // TODO: pre-check the JAR before loading it, if possible
                                        loader.getLogger().debug("Found nested JAR: " + modPath);
                                        Path dest = getInMemoryFs().getPath(UUID.randomUUID() + ".jar");

                                        try {
                                            Files.copy(modPath, dest);
                                        } catch (IOException e) {
                                            throw new RuntimeException("Failed to load nested JAR " + modPath + " into memory (" + dest + ")!", e);
                                        }

                                        list.add(dest);
                                    }
                                });

                        return list;
                    });

                    if (!jarInJars.isEmpty()) {
                        invokeAll(
                                jarInJars.stream()
                                        .map((p) -> {
                                            try {
                                                return new UrlProcessAction(loader, candidatesById, UrlUtil.asUrl(p.normalize()), depth + 1);
                                            } catch (UrlConversionException e) {
                                                throw new RuntimeException("Failed to turn path '" + p.normalize() + "' into URL!", e);
                                            }
                                        }).collect(Collectors.toList())
                        );
                    }
                }
            }

			/* if (jarFs != null) {
				jarFs.close();
			} */
        }
    }


}
