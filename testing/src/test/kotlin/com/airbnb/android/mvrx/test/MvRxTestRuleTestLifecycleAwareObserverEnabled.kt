package com.airbnb.android.mvrx.test

import com.airbnb.mvrx.test.MvRxTestRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class MvRxTestRuleTestLifecycleAwareObserverEnabled {
    @get:Rule
    val mvrxTestRule = MvRxTestRule(setForceDisableLifecycleAwareObserver = false)

    @Test
    fun testLifeCycleAwareObserverEnabled() {
        val view = TestRuleView()
        Assert.assertEquals(0, view.subscribeCallCount)
    }
}