package com.airbnb.android.mvrx.test

import com.airbnb.mvrx.mocking.MockBehavior
import com.airbnb.mvrx.test.MavericksTestRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class MvRxTestRuleTestLifecycleAwareObserverEnabled {
    @get:Rule
    val mavericksTestRule = MavericksTestRule(
        setForceDisableLifecycleAwareObserver = false,
        viewModelMockBehavior = MockBehavior(
            stateStoreBehavior = MockBehavior.StateStoreBehavior.Synchronous,
        ),
    )

    @Test
    fun testLifeCycleAwareObserverEnabled() {
        val view = TestRuleView()
        Assert.assertEquals(0, view.subscribeCallCount)
    }
}
