package net.fabricmc.loader.discovery;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.*;
import net.fabricmc.loader.metadata.AbstractModMetadata;
import net.fabricmc.loader.metadata.EntrypointMetadata;
import net.fabricmc.loader.metadata.LoaderModMetadata;
import net.fabricmc.loader.metadata.NestedJarEntry;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class MixonBuiltinMetadataWrapper extends AbstractModMetadata implements LoaderModMetadata {
    private final ModMetadata parent;

    public MixonBuiltinMetadataWrapper(ModMetadata parent) {
        this.parent = parent;
    }

    public String getType() {
        return this.parent.getType();
    }

    public String getId() {
        return this.parent.getId();
    }

    public Version getVersion() {
        return this.parent.getVersion();
    }

    public ModEnvironment getEnvironment() {
        return this.parent.getEnvironment();
    }

    public Collection<ModDependency> getDepends() {
        return this.parent.getDepends();
    }

    public Collection<ModDependency> getRecommends() {
        return this.parent.getRecommends();
    }

    public Collection<ModDependency> getSuggests() {
        return this.parent.getSuggests();
    }

    public Collection<ModDependency> getConflicts() {
        return this.parent.getConflicts();
    }

    public Collection<ModDependency> getBreaks() {
        return this.parent.getBreaks();
    }

    public String getName() {
        return this.parent.getName();
    }

    public String getDescription() {
        return this.parent.getDescription();
    }

    public Collection<Person> getAuthors() {
        return this.parent.getAuthors();
    }

    public Collection<Person> getContributors() {
        return this.parent.getContributors();
    }

    public ContactInformation getContact() {
        return this.parent.getContact();
    }

    public Collection<String> getLicense() {
        return this.parent.getLicense();
    }

    public Optional<String> getIconPath(int size) {
        return this.parent.getIconPath(size);
    }

    public boolean containsCustomValue(String key) {
        return this.parent.containsCustomValue(key);
    }

    public CustomValue getCustomValue(String key) {
        return this.parent.getCustomValue(key);
    }

    public Map<String, CustomValue> getCustomValues() {
        return this.parent.getCustomValues();
    }

    public int getSchemaVersion() {
        return 2147483647;
    }

    public Map<String, String> getLanguageAdapterDefinitions() {
        return Collections.emptyMap();
    }

    public Collection<NestedJarEntry> getJars() {
        return Collections.emptyList();
    }

    public Collection<String> getMixinConfigs(EnvType type) {
        return Collections.emptyList();
    }

    public String getAccessWidener() {
        return null;
    }

    public boolean loadsInEnvironment(EnvType type) {
        return true;
    }

    public Collection<String> getOldInitializers() {
        return Collections.emptyList();
    }

    public List<EntrypointMetadata> getEntrypoints(String type) {
        return Collections.emptyList();
    }

    public Collection<String> getEntrypointKeys() {
        return Collections.emptyList();
    }

    public void emitFormatWarnings(Logger logger) {
    }
}