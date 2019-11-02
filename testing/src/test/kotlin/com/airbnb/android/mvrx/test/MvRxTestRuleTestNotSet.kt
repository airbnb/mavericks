package com.airbnb.android.mvrx.test

import com.airbnb.mvrx.MvRx
import com.airbnb.mvrx.MvRxViewModelConfigProvider
import com.airbnb.mvrx.test.DebugMode
import com.airbnb.mvrx.test.MvRxTestRule
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
        MvRx.viewModelConfigProvider =
            MvRxViewModelConfigProvider(debugMode = false)
        TestRuleViewModel()
    }

    @Test(expected = OnErrorNotImplementedException::class)
    fun testDebugNoOverride() {
        TestRuleViewModel()
    }
}