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
class MvRxTestRuleTestLifecycleAwareObserverDisabled {
    @get:Rule
    val mvrxTestRule = MvRxTestRule(setForceDisableLifecycleAwareObserver = true)

    @Test
    fun testLifeCycleAwareObserverDisabled() {
        val view = TestRuleView()
        Assert.assertEquals(1, view.subscribeCallCount)
        view.doSomething()
        Assert.assertEquals(2, view.subscribeCallCount)
    }
}