package com.airbnb.mvrx;

@InternalMavericksApi
public class MavericksTestOverrides {
    /**
     * This should only be set by the MvRxTestRule from the mvrx-testing artifact.
     * <p>
     * This can be used to force MavericksViewModels to disable lifecycle aware observer for unit testing.
     * This is Java so it can be package private.
     */
    public static Boolean FORCE_DISABLE_LIFECYCLE_AWARE_OBSERVER = false;

    static boolean FORCE_SYNCHRONOUS_STATE_STORES = false;
}
