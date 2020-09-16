package com.airbnb.mvrx.sample.features.parentfragment

import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.sample.core.MvRxViewModel

data class ParentChildSharedState(val count: Int = 0) : MavericksState

class ParentChildSharedViewModel(state: ParentChildSharedState) : MvRxViewModel<ParentChildSharedState>(state) {
    fun incrementCount() = setState { copy(count = count + 1) }
}
