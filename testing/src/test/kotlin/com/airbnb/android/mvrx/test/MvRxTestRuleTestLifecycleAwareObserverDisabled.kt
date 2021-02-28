package com.airbnb.android.mvrx.test

import com.airbnb.mvrx.test.MvRxTestRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

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