package com.airbnb.mvrx.navigation

import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MvRxState

class NavigationViewModel(initialState: State) : MavericksViewModel<NavigationViewModel.State>(initialState) {

    data class State(
        val producer: String = "",
        val consumer: String = ""
    ) : MvRxState

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
