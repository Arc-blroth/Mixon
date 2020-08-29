package ai.arcblroth.mixon;

import ai.arcblroth.mixon.api.PrePrePreLaunch;
import net.devtech.grossfabrichacks.unsafe.UnsafeUtil;
import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.discovery.*;
import net.fabricmc.loader.gui.FabricGuiEntry;
import net.fabricmc.loader.launch.common.FabricLauncherBase;
import net.fabricmc.loader.metadata.LoaderModMetadata;
import net.fabricmc.loader.util.DefaultLanguageAdapter;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sun.misc.Unsafe;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public final class MixonModLoader {

    protected static final Logger LOGGER = LogManager.getFormatterLogger();
    public static final MixonModLoader INSTANCE = new MixonModLoader();
    private List<BiFunction<String, LoaderModMetadata, LoaderModMetadata>> modMetadataTransformer;
    private List<URL> toBeInjected;
    private List<String> originalModsUpToAndIncludingGFH;
    private List<ModContainer> injectedMods;
    private int previousModListModcount;

    @SuppressWarnings({"unchecked", "unused"})
    public void onPrePrePreLaunch() {
        // entrypoints don't exist yet so let's resolve them ourselves
        FabricLoader.INSTANCE.getAllMods().forEach(mod -> {
            ((net.fabricmc.loader.ModContainer)mod)
                    .getInfo()
                    .getEntrypoints("mixon:prepreprelaunch")
                    .stream()
                    .map(meta -> {
                        try {
                            return ((Class<? extends PrePrePreLaunch>)Class.forName(meta.getValue())).newInstance();
                        } catch (ReflectiveOperationException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .forEach(PrePrePreLaunch::onPrePrePreLaunch);
        });

        this.toBeInjected = MixonModInjectorImpl.INSTANCE.getModsForInjection();
        this.modMetadataTransformer = MixonModInjectorImpl.INSTANCE.getModMetadataTransformers();
        try {
            setup();
            finishModInjecting();
        } catch (Throwable t) {
            FabricGuiEntry.displayCriticalError(t, true);
        }
    }

    private void setup() throws ModResolutionException, ReflectiveOperationException {
        final FabricLoader loader = FabricLoader.INSTANCE;

        // we re-resolve all mods because there's not really a better
        // way to ensure that an injected mod will break something
        ModResolver resolver = new MixonModResolver();
        resolver.addCandidateFinder(new MixonClasspathModCandidateFinder());
        resolver.addCandidateFinder(new DirectoryModCandidateFinder(loader.getModsDir()));
        resolver.addCandidateFinder((owo, addMods) -> toBeInjected.forEach(addMods));

        Map<String, ModCandidate> candidateMap;
        // mod candidate transforming logic is handled
        // below in transformModMetadata
        candidateMap = resolver.resolve(loader);

        List<net.fabricmc.loader.ModContainer> existingMods = MixonFabricLoaderAccessor.getMods(loader);
        Field modCount = AbstractList.class.getDeclaredField("modCount");
        modCount.setAccessible(true);
        previousModListModcount = modCount.getInt(existingMods);

        LOGGER.warn("Some scary-looking warnings may have been generated above, but Mixon and Minecraft should still work. You can ignore those warnings, as they're intended mainly for developers.");

        originalModsUpToAndIncludingGFH = new ArrayList<>();
        originalModsUpToAndIncludingGFH.addAll(
                existingMods.subList(0, existingMods.indexOf(loader.getModContainer("grossfabrichacks").get()) + 1)
                .stream()
                .map(c -> c.getInfo().getId())
                .collect(Collectors.toSet())
        );
        existingMods.clear();
        MixonFabricLoaderAccessor.getModMap(loader).clear();

        String modText;
        switch(candidateMap.values().size()) {
            case 0:
                modText = "Reloading %d mods";
                break;
            case 1:
                modText = "Reloading %d mod: %s";
                break;
            default:
                modText = "Reloading %d mods: %s";
        }

        LOGGER.info("[" + this.getClass().getSimpleName() + "] " + modText, candidateMap.values().size(), candidateMap.values().stream().map((info) -> {
            return String.format("%s@%s", info.getInfo().getId(), info.getInfo().getVersion().getFriendlyString());
        }).collect(Collectors.joining(", ")));

        injectedMods = new ArrayList<>();

        for(String originalModId : originalModsUpToAndIncludingGFH) {
            ModCandidate candidate = candidateMap.get(originalModId);
            if(candidate != null) {
                MixonFabricLoaderAccessor.addMod(loader, candidate);
                if (candidate.getInfo().loadsInEnvironment(loader.getEnvironmentType())) {
                    injectedMods.add(loader.getModContainer(candidate.getInfo().getId()).orElseThrow(() -> new ModResolutionException("Added mod doesn't exist?")));
                } else {
                    throw new ModResolutionException("[Mixon] Cannot change a mod's environment!");
                }
            } else {
                throw new ModResolutionException("[Mixon] Cannot remove a mod!");
            }
        }

        for(ModCandidate candidate : candidateMap.values()) {
            if(!originalModsUpToAndIncludingGFH.contains(candidate.getInfo().getId())) {
                MixonFabricLoaderAccessor.addMod(loader, candidate);
                if (candidate.getInfo().loadsInEnvironment(loader.getEnvironmentType())) {
                    injectedMods.add(loader.getModContainer(candidate.getInfo().getId()).orElseThrow(() -> new ModResolutionException("Added mod doesn't exist?")));
                }
            }
        }
    }

    @SuppressWarnings("unused")
    public static LoaderModMetadata[] transformModMetadata(LoaderModMetadata[] in) {
        for(int i = 0; i < in.length; i++) {
            final int finalI = i;
            INSTANCE.modMetadataTransformer.forEach(f -> in[finalI] = f.apply(in[finalI].getId(), in[finalI]));
        }
        return in;
    }

    private void finishModInjecting() throws NoSuchFieldException, IllegalAccessException {
        final FabricLoader loader = FabricLoader.INSTANCE;

        for(ModContainer mod : injectedMods) {
            net.fabricmc.loader.ModContainer modImpl = (net.fabricmc.loader.ModContainer) mod;

            // add mod
            if (!modImpl.getInfo().getId().equals("fabricloader")) {
                FabricLauncherBase.getLauncher().propose(((net.fabricmc.loader.ModContainer) mod).getOriginUrl());
            }

            // post process metadata
            if (!(modImpl.getInfo().getVersion() instanceof SemanticVersion)) {
                LOGGER.warn("Mod `" + modImpl.getInfo().getId() + "` (" + modImpl.getInfo().getVersion().getFriendlyString() + ") does not respect SemVer - comparison support is limited.");
            } else if (((SemanticVersion) modImpl.getInfo().getVersion()).getVersionComponentCount() >= 4) {
                LOGGER.warn("Mod `" + modImpl.getInfo().getId() + "` (" + modImpl.getInfo().getVersion().getFriendlyString() + ") uses more dot-separated version components than SemVer allows; support for this is currently not guaranteed.");
            }
        }

        // re-add all language adapters up to Mixon
        Map<String, net.fabricmc.loader.ModContainer> modMap = MixonFabricLoaderAccessor.getModMap(loader);
        Map<String, LanguageAdapter> adapterMap = MixonFabricLoaderAccessor.getAdapterMap(loader);
        adapterMap.clear();
        adapterMap.put("default", DefaultLanguageAdapter.INSTANCE);

        for(String modId : originalModsUpToAndIncludingGFH) {
            net.fabricmc.loader.ModContainer mod = modMap.get(modId);
            for(Map.Entry<String, String> laEntry : mod.getInfo().getLanguageAdapterDefinitions().entrySet()) {
                if (adapterMap.containsKey(laEntry.getKey())) {
                    throw new RuntimeException("Duplicate language adapter key: " + laEntry.getKey() + "! (" + laEntry.getValue() + ", " + ((LanguageAdapter)adapterMap.get(laEntry.getKey())).getClass().getName() + ")");
                }

                try {
                    adapterMap.put(laEntry.getKey(), (LanguageAdapter)Class.forName(laEntry.getValue(), true, FabricLauncherBase.getLauncher().getTargetClassLoader()).getDeclaredConstructor().newInstance());
                } catch (Exception var6) {
                    throw new RuntimeException("Failed to instantiate language adapter: " + laEntry.getKey(), var6);
                }
            }
        }

        // at this point returning would mean an instant ConcurrentModificationException
        // so we convince the mods iterator that everything is fine
        List<net.fabricmc.loader.ModContainer> mods = MixonFabricLoaderAccessor.getMods(loader);
        Field modCount = AbstractList.class.getDeclaredField("modCount");
        modCount.setAccessible(true);
        modCount.setInt(mods, previousModListModcount);
    }

}
