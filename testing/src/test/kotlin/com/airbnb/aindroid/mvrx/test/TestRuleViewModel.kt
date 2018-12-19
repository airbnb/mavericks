package com.airbnb.aindroid.mvrx.test

import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.MvRxState

data class TestRuleState(val foo: String = "hello") : MvRxState
class TestRuleViewModel(debugMode: Boolean = false) : BaseMvRxViewModel<TestRuleState>(TestRuleState(), debugMode) {

    var subscribeCallCount = 0

    init {
        subscribe {
            subscribeCallCount++
        }
    }

    fun doSomething() = setState { copy(foo = "$foo!") }
}

