package com.airbnb.mvrx;

public class MvRxTestOverrides {
    /**
     * This should only be set by the MvRxTestRule from the mvrx-testing artifact.
     *
     * This can be used to force MvRxViewModels to be or not to be in debug mode for tests.
     * This is Java so it can be package private.
     */
    static Boolean FORCE_DEBUG = null;

    /**
     * This should only be set by the MvRxTestRule from the mvrx-testing artifact.
     *
     * This can be used to force MvRxViewModels to disable lifecycle aware observer for unit testing.
     * This is Java so it can be package private.
     */
    static Boolean FORCE_DISABLE_LIFECYCLE_AWARE_OBSERVER = false;
}
