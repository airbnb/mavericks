package com.airbnb.android.mvrx.test

import com.airbnb.mvrx.test.DebugMode
import com.airbnb.mvrx.test.MvRxTestRule
import io.reactivex.exceptions.OnErrorNotImplementedException
import org.junit.Assert.assertEquals
import org.junit.ClassRule
import org.junit.Test

class MvRxTestRuleTestNotSet {
    companion object {
        @JvmField
        @ClassRule
        val mvrxTestRule = MvRxTestRule(DebugMode.Unset, setRxImmediateSchedulers = true)
    }

    @Test
    fun testNotDebugNoOverride() {
        val viewModel = TestRuleViewModel()
        viewModel.doSomething()
        assertEquals(1, viewModel.setStateCount)
    }

    fun testDebugNoOverride() {
        val viewModel = TestRuleViewModel(true)
        viewModel.doSomething()
        assertEquals(2, viewModel.setStateCount)
    }
}