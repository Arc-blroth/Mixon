package ai.arcblroth.mixon;

import ai.arcblroth.mixon.api.MixonModInjector;
import net.fabricmc.loader.metadata.LoaderModMetadata;
import net.fabricmc.loader.util.UrlUtil;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

public class MixonModInjectorImpl implements MixonModInjector {

    public static final MixonModInjectorImpl INSTANCE = new MixonModInjectorImpl();
    private final ArrayList<URL> modsToInject = new ArrayList<>();
    private final ArrayList<BiFunction<String, LoaderModMetadata, LoaderModMetadata>> candidateTransformers = new ArrayList<>();

    private MixonModInjectorImpl() {

    }

    @Override
    public void addMod(URL source) {
        modsToInject.add(source);
    }

    @Override
    public void addMod(File source) {
        try {
            modsToInject.add(UrlUtil.asUrl(source));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addModMetadataTransformer(BiFunction<String, LoaderModMetadata, LoaderModMetadata> transformer) {
        this.candidateTransformers.add(transformer);
    }

    public List<URL> getModsForInjection() {
        return Collections.unmodifiableList(this.modsToInject);
    }

    public List<BiFunction<String, LoaderModMetadata, LoaderModMetadata>> getModMetadataTransformers() {
        return Collections.unmodifiableList(this.candidateTransformers);
    }

}
