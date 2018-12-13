package com.airbnb.mvrx;

public class MvRxTestOverrides {
    /**
     * This should only be set by the MvRxTestRule from the mvrx-testing artifact.
     *
     * This can be used to force MvRxViewModels to be or not to be in debug mode for tests.
     * This is Java so it can be packgage private.
     */
    static Boolean FORCE_DEBUG = null;
}
