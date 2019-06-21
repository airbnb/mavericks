package com.airbnb.mvrx.sample.features.statefulview

import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.sample.core.MvRxViewModel

data class StatefulViewState(val title: String = "Hello World") : MvRxState

class StatefulViewViewModel(state: StatefulViewState) : MvRxViewModel<StatefulViewState>(state) {
    fun setTitle(title: String) = setState { copy(title = title) }
}