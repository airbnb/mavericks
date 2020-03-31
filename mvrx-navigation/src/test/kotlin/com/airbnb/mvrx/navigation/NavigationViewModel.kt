package com.airbnb.mvrx.navigation

import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.MvRxState

class NavigationViewModel(initialState: State) : BaseMvRxViewModel<NavigationViewModel.State>(initialState, false) {

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
