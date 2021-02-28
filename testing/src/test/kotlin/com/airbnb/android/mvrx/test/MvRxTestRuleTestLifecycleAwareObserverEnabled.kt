package com.airbnb.android.mvrx.test

import com.airbnb.mvrx.test.MvRxTestRule
import org.junit.Assert
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@Suppress("DEPRECATION")
@RunWith(RobolectricTestRunner::class)
@LooperMode(LooperMode.Mode.LEGACY)
@Config(sdk = [28]) // SDK 29 required Java 9+
class MvRxTestRuleTestLifecycleAwareObserverEnabled {
    @get:Rule
    val mvrxTestRule = MvRxTestRule(setForceDisableLifecycleAwareObserver = false)

    @Test
    fun testLifeCycleAwareObserverEnabled() {
        val view = TestRuleView()
        Assert.assertEquals(0, view.subscribeCallCount)
    }
}