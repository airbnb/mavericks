package com.airbnb.mvrx;

import androidx.annotation.RestrictTo;

import com.airbnb.mvrx.test.MvRxTestRule;

/**
 * Used as a proxy between {@link MvRxTestRule} and MvRx.
 * this is Java because the flag is package private.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class MvRxTestOverridesProxy {
    public static void forceMvRxDebug(Boolean debug) {
        MvRxTestOverrides.FORCE_DEBUG = debug;
    }

    public static void forceDisableLifecycleAwareObserver(Boolean disableLifecycleAwareObserver) {
        MvRxTestOverrides.FORCE_DISABLE_LIFECYCLE_AWARE_OBSERVER = disableLifecycleAwareObserver;
    }
}
