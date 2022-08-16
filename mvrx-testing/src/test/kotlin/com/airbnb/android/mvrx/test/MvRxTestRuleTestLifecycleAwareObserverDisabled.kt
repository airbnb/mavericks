package com.airbnb.android.mvrx.test

import com.airbnb.mvrx.mocking.MockBehavior
import com.airbnb.mvrx.test.MavericksTestRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class MvRxTestRuleTestLifecycleAwareObserverDisabled {
    @get:Rule
    val mvrxTestRule = MavericksTestRule(
        setForceDisableLifecycleAwareObserver = true,
        viewModelMockBehavior = MockBehavior(
            stateStoreBehavior = MockBehavior.StateStoreBehavior.Synchronous,
        ),
    )

    @Test
    fun testLifeCycleAwareObserverDisabled() {
        val view = TestRuleView()
        Assert.assertEquals(1, view.subscribeCallCount)
        view.doSomething()
        Assert.assertEquals(2, view.subscribeCallCount)
    }
}
