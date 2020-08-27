package ai.arcblroth.mixon.api;

import ai.arcblroth.mixon.MixonModInjectorImpl;
import net.fabricmc.loader.discovery.ModCandidate;
import net.fabricmc.loader.metadata.LoaderModMetadata;

import java.io.File;
import java.net.URL;
import java.util.function.BiFunction;

public interface MixonModInjector {

    public static MixonModInjector getInstance() {
        return MixonModInjectorImpl.INSTANCE;
    }

    public void addMod(URL source);

    public void addMod(File source);

    public void addModMetadataTransformer(BiFunction<String, LoaderModMetadata, LoaderModMetadata> transformer);

}
