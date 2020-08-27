package ai.arcblroth.mixon;

import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.api.LanguageAdapterException;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.util.DefaultLanguageAdapter;

public class MixonFauxLanguageAdapter implements LanguageAdapter {

    private static final String MOD_METADATA_PARSER_CLASS_NAME = "net.fabricmc.loader.metadata.ModMetadataParser";

    static {
        MixonModLoader.INSTANCE.onPrePrePreLaunch();
    }

    @Override
    public <T> T create(ModContainer modContainer, String s, Class<T> aClass) throws LanguageAdapterException {
        MixonModLoader.LOGGER.error("How did you even find this entrypoint!?");
        return DefaultLanguageAdapter.INSTANCE.create(modContainer, s, aClass);
    }

    /* Why copy paste when you can over-complicate the problem? **/
    //private static class InjectorClassLoader extends ClassLoader {
    //
    //    private final HashMap<String, byte[]> patchList = new HashMap<>();
    //    private final ArrayList<String> forceLoadList = new ArrayList<>();
    //
    //    public InjectorClassLoader(ClassLoader parent) {
    //        super(parent);
    //    }
    //
    //    public void addPatch(String className, byte[] source) {
    //        this.patchList.put(className, source);
    //    }
    //
    //    public void addForceLoaded(String className) {
    //        this.forceLoadList.add(className);
    //    }
    //
    //    @Override
    //    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    //        if(this.patchList.containsKey(name)) {
    //            synchronized (getClassLoadingLock(name)) {
    //                byte[] src = this.patchList.get(name);
    //                Class<?> c = super.defineClass(name, src, 0, src.length);
    //                if(resolve) {
    //                    super.resolveClass(c);
    //                }
    //                return c;
    //            }
    //        } else {
    //            if(this.forceLoadList.contains(name)) {
    //                try(InputStream stream = getParent().getResourceAsStream(name.replace(".", "/").concat(".class"))) {
    //                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    //                    byte[] buffer = new byte[2048];
    //                    int read = -1;
    //                    while((read = stream.read(buffer)) != -1) {
    //                        baos.write(buffer, 0, read);
    //                    }
    //                    byte[] src = baos.toByteArray();
    //                    Class<?> c = super.defineClass(name, src, 0, src.length, this.getClass().getProtectionDomain());
    //                    if(resolve) {
    //                        super.resolveClass(c);
    //                    }
    //                    return c;
    //                } catch (IOException e) {
    //                    throw new ClassNotFoundException(name, e);
    //                }
    //            }
    //            return super.loadClass(name, resolve);
    //        }
    //    }
    //}

}
