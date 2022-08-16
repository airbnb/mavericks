package com.airbnb.mvrx;

/**
 * Used as a proxy between {@link com.airbnb.mvrx.test.MavericksTestRule} and Mavericks.
 * this is Java because the flag is package private.
 */
@InternalMavericksApi
public class MvRxTestOverridesProxy {

    public static void forceDisableLifecycleAwareObserver(Boolean disableLifecycleAwareObserver) {
        MavericksTestOverrides.FORCE_DISABLE_LIFECYCLE_AWARE_OBSERVER = disableLifecycleAwareObserver;
    }
}
