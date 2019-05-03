package com.airbnb.android.mvrx.test

import com.airbnb.mvrx.test.MvRxTestRule
import org.junit.Assert
import org.junit.ClassRule
import org.junit.Test

class MvRxTestRuleTestLifecycleAwareObserverDisabled {
    companion object {
        @JvmField
        @ClassRule
        val mvrxTestRule = MvRxTestRule(setForceDisableLifecycleAwareObserver = true)
    }

    @Test
    fun testLifeCycleAwareObserverDisabled() {
        val view = TestRuleView()
        Assert.assertEquals(1, view.subscribeCallCount)
        view.doSomething()
        Assert.assertEquals(2, view.subscribeCallCount)
    }
}