package com.airbnb.mvrx.navigation

import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel

class NavigationViewModel(initialState: State) : MavericksViewModel<NavigationViewModel.State>(initialState) {

    data class State(
        val producer: String = "",
        val consumer: String = ""
    ) : MavericksState

    fun updateProducer(value: String) {
        setState {
            copy(producer = value)
        }
    }

    fun updateConsumer(value: String) {
        setState {
            copy(consumer = value)
        }
    }
}
