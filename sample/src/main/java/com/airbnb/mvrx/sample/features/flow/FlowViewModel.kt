package com.airbnb.mvrx.sample.features.flow

import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.PersistState
import com.airbnb.mvrx.sample.core.MvRxViewModel

/**
 * [PersistState] will persist the count if Android kills the process in the background
 * and restores it in a new process.
 */
data class FlowState(@PersistState val count: Int = 0) : MvRxState

class FlowViewModel(initialState: FlowState) : MvRxViewModel<FlowState>(initialState) {

    fun setCount(count: Int) = setState { copy(count = count) }
}