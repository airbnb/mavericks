package com.airbnb.mvrx;

import android.support.annotation.RestrictTo;

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
}
