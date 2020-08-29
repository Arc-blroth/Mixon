package ai.arcblroth.mixon.api;

/**
 * The earliest possible entrypoint, injected with a class initialization block
 * in a faux language adapter. This used to be done in MixonFauxLanguageAdapter,
 * but the API has now been merged into GrossFabricHacks. It is recommended to
 * use this entrypoint rather than GFH's to ensure that any transformations
 * you make are actually loaded by Mixon.<br>
 * Do NOT directly reference any game or other mod classes from this entrypoint.<br>
 * All Throwables propagated from here will be displayed as a critical mod loading error.
 *
 * @see MixonModInjector
 * @author Arc'🅱️lroth
 */
@FunctionalInterface
public interface PrePrePreLaunch {

    void onPrePrePreLaunch();

}
