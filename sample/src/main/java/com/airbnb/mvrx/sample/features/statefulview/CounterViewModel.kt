package com.airbnb.mvrx.sample.features.statefulview

import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.PersistState
import com.airbnb.mvrx.sample.core.MvRxViewModel

data class CounterState(@PersistState val count: Int = 0) : MvRxState

class CounterViewModel(state: CounterState) : MvRxViewModel<CounterState>(state) {

    fun incrementCount() = setState { copy(count = count + 1) }
}