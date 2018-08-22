package com.airbnb.mvrx.sample.features

import com.airbnb.epoxy.EpoxyController
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.sample.core.BaseFragment
import com.airbnb.mvrx.sample.core.MvRxViewModel
import com.airbnb.mvrx.sample.views.marquee
import com.airbnb.mvrx.withState

data class HelloWorldState(val title: String = "Hello World") : MvRxState

class HelloWorldViewModel(initialState: HelloWorldState) : MvRxViewModel<HelloWorldState>(initialState)

class HelloWorldFragment : BaseFragment() {
    private val viewModel by fragmentViewModel(HelloWorldViewModel::class)

    override fun EpoxyController.buildModels() = withState(viewModel) { state ->
        marquee {
            id("marquee")
            title(state.title)
        }
    }
}
