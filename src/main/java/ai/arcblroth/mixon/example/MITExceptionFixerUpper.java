package ai.arcblroth.mixon.example;

import ai.arcblroth.mixon.api.LoaderModMetadataWrapper;
import ai.arcblroth.mixon.api.MixonModInjector;
import ai.arcblroth.mixon.api.PrePrePreLaunch;
import net.fabricmc.loader.metadata.EntrypointMetadata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class MITExceptionFixerUpper implements PrePrePreLaunch {

    public static final boolean ENABLE = true;

    @Override
    public void onPrePrePreLaunch() {
        if(!ENABLE) return;

        try {
            File localJar = new File("mitexception-1.0.0.jar");
            if(!localJar.exists()) {
                final URL url = new URL("https://github.com/Maowcraft/MITException/releases/download/1.0.0/mitexception-1.0.0.jar");
                ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                FileOutputStream fos = new FileOutputStream(localJar);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            }
            MixonModInjector.getInstance().addMod(localJar);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        MixonModInjector.getInstance().addModMetadataTransformer((id, metadata) -> {
            if(metadata.getLicense().stream().anyMatch(s -> s.toLowerCase().equals("mit"))) {
                return new LoaderModMetadataWrapper(metadata) {
                    @Override
                    public List<EntrypointMetadata> getEntrypoints(String s) {
                        if(this.getId().equals("mitexception")) {
                            return new ArrayList<>();
                        } else {
                            return super.getEntrypoints(s);
                        }
                    }

                    @Override
                    public String getDescription() {
                        if(this.getId().equals("mitexception")) {
                            return "try {\n" + super.getDescription() + "\n} catch (MITException e) {\n    // Mwahaha\n}";
                        } else {
                            return super.getDescription();
                        }
                    }
                };
            } else {
                return metadata;
            }
        });
    }

}
