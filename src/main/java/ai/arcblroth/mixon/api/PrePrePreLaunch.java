package ai.arcblroth.mixon.api;

/**
 * The earliest possible entrypoint.
 * Use this entrypoint with the MixonModInjector API.
 * Do NOT directly reference any game or other mod classes from this entrypoint.<br>
 * All Throwables propagated from here will be displayed as a critical mod loading error.
 */
@FunctionalInterface
public interface PrePrePreLaunch {

    void onPrePrePreLaunch();

}
