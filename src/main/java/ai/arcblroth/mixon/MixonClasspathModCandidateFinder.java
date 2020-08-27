package ai.arcblroth.mixon;

import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.discovery.ModCandidateFinder;
import net.fabricmc.loader.launch.common.FabricLauncherBase;
import net.fabricmc.loader.util.UrlConversionException;
import net.fabricmc.loader.util.UrlUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

// This class exists solely for a 1 line change
public class MixonClasspathModCandidateFinder implements ModCandidateFinder {

    @Override
    public void findCandidates(FabricLoader loader, Consumer<URL> appender) {
        Stream<URL> urls;

        if (FabricLauncherBase.getLauncher().isDevelopment()) {
            try {
                // ----[ BEGIN MIXON STUFF ]----
                Enumeration<URL> mods = FabricLauncherBase.class.getClassLoader().getResources("fabric.mod.json");
                // ----[ END MIXON STUFF ]----
                Set<URL> modsList = new HashSet<>();
                while (mods.hasMoreElements()) {
                    try {
                        modsList.add(UrlUtil.getSource("fabric.mod.json", mods.nextElement()));
                    } catch (UrlConversionException e) {
                        loader.getLogger().debug(e);
                    }
                }
                loader.getLogger().debug("[InjectorClasspathModCandidateFinder] Adding dev classpath directories to classpath.");
                String[] classpathPropertyInput = System.getProperty("java.class.path", "").split(File.pathSeparator);
                for (String s : classpathPropertyInput) {
                    if (s.isEmpty() || s.equals("*") || s.endsWith(File.separator + "*")) continue;
                    File file = new File(s);
                    if (file.exists() && file.isDirectory()) {
                        try {
                            URL url = UrlUtil.asUrl(file);
                            if (!modsList.contains(url)) {
                                FabricLauncherBase.getLauncher().propose(url);
                            }
                        } catch (UrlConversionException e) {
                            loader.getLogger().warn("[InjectorClasspathModCandidateFinder] Failed to add dev directory " + file.getAbsolutePath() + " to classpath!", e);
                        }
                    }
                }

                urls = modsList.stream();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                urls = Stream.of(FabricLauncherBase.getLauncher().getClass().getProtectionDomain().getCodeSource().getLocation());
            } catch (Throwable t) {
                loader.getLogger().debug("Could not fallback to itself for mod candidate lookup!", t);
                urls = Stream.empty();
            }
        }

        urls.forEach((url) -> {
            loader.getLogger().debug("[InjectorClasspathModCandidateFinder] Processing " + url.getPath());
            File f;
            try {
                f = UrlUtil.asFile(url);
            } catch (UrlConversionException e) {
                return;
            }

            if (f.exists()) {
                if (f.isDirectory() || f.getName().endsWith(".jar")) {
                    appender.accept(url);
                }
            }
        });
    }

}
