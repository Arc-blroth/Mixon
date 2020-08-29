# Mixon & Mixaround

A dynamic mod injector and fabric.mod.json transformer system 

# Usage

Add the "mixon:prepreprelaunch" entrypoint to your fabric.mod.json,
then use the methods in [MixonModInjector](src/main/java/ai/arcblroth/mixon/api/MixonModInjector.java).
For an example,
see the [MITExceptionFixerUpper](src/main/java/ai/arcblroth/mixon/example/MITExceptionFixerUpper.java) class.

### Notes on Licensing

The classes MixonBuiltinMetadataWrapper, MixonModResolver, and MixonClasspathModCandidateFinder
contain code taken from https://github.com/FabricMC/Fabric-Loader.
Major modifications are wrapped in
`// ----[ BEGIN MIXON STUFF ]----` and `// ----[ END MIXON STUFF ]----`
style comments.