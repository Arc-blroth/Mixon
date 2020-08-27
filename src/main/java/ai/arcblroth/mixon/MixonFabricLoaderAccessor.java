package ai.arcblroth.mixon;

import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.ModContainer;
import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.discovery.ModCandidate;
import net.fabricmc.loader.discovery.ModResolutionException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class MixonFabricLoaderAccessor {

    private static Method addMod = null;

    public static void addMod(FabricLoader loader, ModCandidate candidate) throws ModResolutionException {
        try {
            if(addMod == null) {
                addMod = loader.getClass().getDeclaredMethod("addMod", ModCandidate.class);
                addMod.setAccessible(true);
            }
            addMod.invoke(loader, candidate);
        } catch (InvocationTargetException e) {
            if(e.getCause() instanceof ModResolutionException) {
                throw (ModResolutionException)e.getCause();
            } else {
                throw new RuntimeException(e);
            }
        }
        catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, LanguageAdapter> getAdapterMap(FabricLoader loader) {
        try {
            Field adapterMap = loader.getClass().getDeclaredField("adapterMap");
            adapterMap.setAccessible(true);
            return (Map<String, LanguageAdapter>) adapterMap.get(loader);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<ModContainer> getMods(FabricLoader loader) {
        try {
            Field mods = loader.getClass().getDeclaredField("mods");
            mods.setAccessible(true);
            return ((List<ModContainer>)mods.get(loader));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }


    @SuppressWarnings("unchecked")
    public static Map<String, ModContainer> getModMap(FabricLoader loader) {
        try {
            Field mods = loader.getClass().getDeclaredField("modMap");
            mods.setAccessible(true);
            return ((Map<String, ModContainer>)mods.get(loader));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

}
