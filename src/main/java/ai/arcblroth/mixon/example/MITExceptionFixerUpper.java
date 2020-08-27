package ai.arcblroth.mixon.example;

import ai.arcblroth.mixon.api.LoaderModMetadataWrapper;
import ai.arcblroth.mixon.api.MixonModInjector;
import ai.arcblroth.mixon.api.PrePrePreLaunch;
import net.fabricmc.loader.discovery.ModCandidate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Collection;
import java.util.stream.Collectors;

public class MITExceptionFixerUpper implements PrePrePreLaunch {

    private static final String mitExceptionUrl = "https://github.com/Maowcraft/MITException/releases/download/1.0.0/mitexception-1.0.0.jar";
    private static final String mitExceptionLocalFile = "mitexception-1.0.0.jar";

    @Override
    public void onPrePrePreLaunch() {
        try {
            File localJar = new File(mitExceptionLocalFile);
            if(!localJar.exists()) {
                final URL url = new URL(mitExceptionUrl);
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
                    public Collection<String> getLicense() {
                        return super.getLicense().stream()
                                .map(s -> s.toLowerCase().equals("mit") ? "mit " : s)
                                .collect(Collectors.toList());
                    }
                };
            } else {
                return metadata;
            }
        });
    }

}
