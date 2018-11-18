package com.airbnb.mvrx.sample.features.flow

import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.sample.core.MvRxViewModel
import javax.inject.Inject

/**
 * Shared between [FlowIntroFragment] and [FlowCounterFragment]
 */
data class FlowState(val count: Int = 0) : MvRxState

class FlowViewModel @Inject constructor(): MvRxViewModel<FlowState>(FlowState()) {

    fun setCount(count: Int) = setState { copy(count = count) }
}