package com.airbnb.android.mvrx.test

import com.airbnb.mvrx.MvRx
import com.airbnb.mvrx.mocking.MockMvRxViewModelConfigFactory
import com.airbnb.mvrx.test.MvRxTestRule
import org.junit.Rule
import org.junit.Test

class MvRxTestRuleMockBehaviorTest {
    @get:Rule
    val mvrxTestRule = MvRxTestRule()

    @Test
    fun mockingDisabledFromNullBehavior() {
        check(MvRx.viewModelConfigFactory is com.airbnb.mvrx.mocking.MockMvRxViewModelConfigFactory)
    }
}

class MvRxTestRuleNoMockBehaviorTest {

    @get:Rule
    val mvrxTestRule = MvRxTestRule(viewModelMockBehavior = null)

    @Test
    fun mockingDisabledFromNullBehavior() {
        check(MvRx.viewModelConfigFactory !is com.airbnb.mvrx.mocking.MockMvRxViewModelConfigFactory)
    }

}
