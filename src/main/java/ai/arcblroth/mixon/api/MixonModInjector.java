package ai.arcblroth.mixon.api;

import ai.arcblroth.mixon.MixonModInjectorImpl;
import net.fabricmc.loader.metadata.LoaderModMetadata;

import java.io.File;
import java.net.URL;
import java.util.function.BiFunction;

public interface MixonModInjector {

    public static MixonModInjector getInstance() {
        return MixonModInjectorImpl.INSTANCE;
    }

    /**
     * Adds a mod from a local URL.
     * This will not work with http:// or https:// URLs!
     * @param source source url
     */
    public void addMod(URL source);

    /**
     * Adds a mod from a local file.
     * @param source source file
     */
    public void addMod(File source);

    /**
     * Adds a LoaderModMetadata transformer.
     * This allows you to modify the fabric.mod.json metadata
     * from other mods. Note that you cannot use this to
     * change the environment of the mod in such a way that
     * the mod is not loaded, eg you cannot change a server only
     * environment to a client only environment on a server.
     * @param transformer function that takes in the id of the mod and its metadata, and returns either the original metadata or a new metadata
     * @see LoaderModMetadataWrapper
     */
    public void addModMetadataTransformer(BiFunction<String, LoaderModMetadata, LoaderModMetadata> transformer);

}
