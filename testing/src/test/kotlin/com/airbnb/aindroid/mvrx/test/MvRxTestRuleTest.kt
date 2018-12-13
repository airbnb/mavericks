package com.airbnb.aindroid.mvrx.test

import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.test.DebugMode
import com.airbnb.mvrx.test.MvRxTestRule
import org.junit.Rule
import org.junit.Test

data class TestRuleState(var foo: String = "hello") : MvRxState
class TestRuleViewModel(debugMode: Boolean = false) : BaseMvRxViewModel<TestRuleState>(TestRuleState(), debugMode)

class MvRxTestRuleTestNotSet {
    @JvmField @Rule
    val mvrxTestRule = MvRxTestRule(DebugMode.Unset, setRxImmediateSchedulers = true)

    @Test
    fun testNotDebugNoOverride() {
        TestRuleViewModel()
    }

    @Test(expected = Exception::class)
    fun testDebugNoOverride() {
        TestRuleViewModel(true)
    }
}

class MvRxTestRuleTestNotDebug {
    @JvmField @Rule
    val mvrxTestRule = MvRxTestRule(DebugMode.NotDebug, setRxImmediateSchedulers = true)

    @Test
    fun testNotDebugIsSet() {
        TestRuleViewModel()
    }
}

class MvRxTestRuleTestDebug {
    @JvmField @Rule
    val mvrxTestRule = MvRxTestRule(DebugMode.Debug, setRxImmediateSchedulers = true)

    @Test(expected = Exception::class)
    fun testDebugIsSet() {
        TestRuleViewModel()
    }
}