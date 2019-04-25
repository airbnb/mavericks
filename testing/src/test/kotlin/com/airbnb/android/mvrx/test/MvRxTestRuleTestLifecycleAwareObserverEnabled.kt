package com.airbnb.android.mvrx.test

import com.airbnb.mvrx.test.MvRxTestRule
import org.junit.Assert
import org.junit.ClassRule
import org.junit.Test

class MvRxTestRuleTestLifecycleAwareObserverEnabled {
    companion object {
        @JvmField
        @ClassRule
        val mvrxTestRule = MvRxTestRule(setForceDisableLifecycleAwareObserver = false)
    }

    @Test
    fun testLifeCycleAwareObserverEnabled() {
        val view = TestRuleView()
        Assert.assertEquals(0, view.subscribeCallCount)
    }
}