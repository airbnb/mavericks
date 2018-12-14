package com.airbnb.aindroid.mvrx.test

import com.airbnb.mvrx.test.DebugMode
import com.airbnb.mvrx.test.MvRxTestRule
import org.junit.Assert
import org.junit.ClassRule
import org.junit.Test

class MvRxTestRuleTestNotDebug {
    companion object {
        @JvmField
        @ClassRule
        val mvrxTestRule = MvRxTestRule(DebugMode.NotDebug, setRxImmediateSchedulers = true)
    }

    @Test
    fun testNotDebugIsSet() {
        TestRuleViewModel()
    }

    @Test
    fun testImmediateSchedulers() {
        val viewModel = TestRuleViewModel()
        Assert.assertEquals(1, viewModel.subscribeCallCount)
        viewModel.doSomething()
        Assert.assertEquals(2, viewModel.subscribeCallCount)
    }
}