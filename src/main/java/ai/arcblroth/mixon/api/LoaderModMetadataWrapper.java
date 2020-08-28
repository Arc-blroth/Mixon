package ai.arcblroth.mixon.api;

import com.google.gson.JsonElement;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.*;
import net.fabricmc.loader.metadata.EntrypointMetadata;
import net.fabricmc.loader.metadata.LoaderModMetadata;
import net.fabricmc.loader.metadata.NestedJarEntry;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A LoaderModMetadata implementation that defers to a parent LoaderModMetadata by default.
 */
public abstract class LoaderModMetadataWrapper implements LoaderModMetadata  {

    private final LoaderModMetadata parent;

    public LoaderModMetadataWrapper(LoaderModMetadata parent) {
        this.parent = parent;
    }

    @Override
    public int getSchemaVersion() {
        return parent.getSchemaVersion();
    }

    @Override
    public Map<String, String> getLanguageAdapterDefinitions() {
        return parent.getLanguageAdapterDefinitions();
    }

    @Override
    public Collection<NestedJarEntry> getJars() {
        return parent.getJars();
    }

    @Override
    public Collection<String> getMixinConfigs(EnvType envType) {
        return parent.getMixinConfigs(envType);
    }

    @Override
    public String getAccessWidener() {
        return parent.getAccessWidener();
    }

    @Override
    public boolean loadsInEnvironment(EnvType envType) {
        return parent.loadsInEnvironment(envType);
    }

    @Override
    public Collection<String> getOldInitializers() {
        return parent.getOldInitializers();
    }

    @Override
    public List<EntrypointMetadata> getEntrypoints(String s) {
        return parent.getEntrypoints(s);
    }

    @Override
    public Collection<String> getEntrypointKeys() {
        return parent.getEntrypointKeys();
    }

    @Override
    public void emitFormatWarnings(Logger logger) {
        parent.emitFormatWarnings(logger);
    }

    @Override
    public String getType() {
        return parent.getType();
    }

    @Override
    public String getId() {
        return parent.getId();
    }

    @Override
    public Version getVersion() {
        return parent.getVersion();
    }

    @Override
    public ModEnvironment getEnvironment() {
        return parent.getEnvironment();
    }

    @Override
    public Collection<ModDependency> getDepends() {
        return parent.getDepends();
    }

    @Override
    public Collection<ModDependency> getRecommends() {
        return parent.getRecommends();
    }

    @Override
    public Collection<ModDependency> getSuggests() {
        return parent.getSuggests();
    }

    @Override
    public Collection<ModDependency> getConflicts() {
        return parent.getConflicts();
    }

    @Override
    public Collection<ModDependency> getBreaks() {
        return parent.getBreaks();
    }

    @Override
    public String getName() {
        return parent.getName();
    }

    @Override
    public String getDescription() {
        return parent.getDescription();
    }

    @Override
    public Collection<Person> getAuthors() {
        return parent.getAuthors();
    }

    @Override
    public Collection<Person> getContributors() {
        return parent.getContributors();
    }

    @Override
    public ContactInformation getContact() {
        return parent.getContact();
    }

    @Override
    public Collection<String> getLicense() {
        return parent.getLicense();
    }

    @Override
    public Optional<String> getIconPath(int i) {
        return parent.getIconPath(i);
    }

    @Override
    public boolean containsCustomValue(String s) {
        return parent.containsCustomValue(s);
    }

    @Override
    public CustomValue getCustomValue(String s) {
        return parent.getCustomValue(s);
    }

    @Override
    public Map<String, CustomValue> getCustomValues() {
        return parent.getCustomValues();
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean containsCustomElement(String s) {
        return parent.containsCustomElement(s);
    }

    @Override
    @SuppressWarnings("deprecation")
    public JsonElement getCustomElement(String s) {
        return parent.getCustomElement(s);
    }

}
