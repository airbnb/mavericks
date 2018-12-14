package com.airbnb.aindroid.mvrx.test

import com.airbnb.mvrx.test.DebugMode
import com.airbnb.mvrx.test.MvRxTestRule
import io.reactivex.exceptions.CompositeException
import org.junit.ClassRule
import org.junit.Test

class MvRxTestRuleTestDebug {
    companion object {
        @JvmField
        @ClassRule
        val mvrxTestRule = MvRxTestRule(DebugMode.Debug, setRxImmediateSchedulers = true)
    }

    @Test(expected = CompositeException::class)
    fun testDebugIsSet() {
        TestRuleViewModel()
    }
}