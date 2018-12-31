package com.airbnb.aindroid.mvrx.test

import com.airbnb.mvrx.test.DebugMode
import com.airbnb.mvrx.test.MvRxTestRule
import io.reactivex.exceptions.CompositeException
import io.reactivex.exceptions.OnErrorNotImplementedException
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
        TestRuleViewModel()
    }

    @Test(expected = OnErrorNotImplementedException::class)
    fun testDebugNoOverride() {
        TestRuleViewModel(true)
    }
}