package com.airbnb.mvrx.sample.features.flow

import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.sample.core.MvRxViewModel

data class FlowState(val count: Int = 0) : MvRxState

class FlowViewModel(override val initialState: FlowState) : MvRxViewModel<FlowState>() {

    fun setCount(count: Int) = setState { copy(count = count) }

    fun incrementCount() = setState { copy(count = count + 1) }
}