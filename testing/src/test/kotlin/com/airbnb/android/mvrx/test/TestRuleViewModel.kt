package com.airbnb.android.mvrx.test

import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.MavericksState

data class TestRuleState(val foo: String = "hello") : MavericksState
class TestRuleViewModel() : BaseMvRxViewModel<TestRuleState>(TestRuleState()) {

    var subscribeCallCount = 0
    var setStateCount = 0

    init {
        subscribe {
            subscribeCallCount++
        }
    }

    fun doSomething() = setState {
        setStateCount++
        copy(foo = "$foo!")
    }
}

