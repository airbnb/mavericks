package com.airbnb.android.mvrx.test

import com.airbnb.mvrx.test.DebugMode
import com.airbnb.mvrx.test.MvRxTestRule
import io.reactivex.exceptions.OnErrorNotImplementedException
import org.junit.Assert.assertEquals
import org.junit.ClassRule
import org.junit.Test

class MvRxTestRuleTestDebug {
    companion object {
        @JvmField
        @ClassRule
        val mvrxTestRule = MvRxTestRule(DebugMode.Debug, setRxImmediateSchedulers = true)
    }

    @Test
    fun testDebugIsOverriddenByTestRule() {
        val viewModel = TestRuleViewModel(debugMode = false)
        viewModel.doSomething()
        assertEquals(2, viewModel.setStateCount)
    }
}