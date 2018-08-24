package com.airbnb.mvrx.sample.features.helloworld

import com.airbnb.epoxy.EpoxyController
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.sample.core.BaseFragment
import com.airbnb.mvrx.sample.views.marquee
import com.airbnb.mvrx.withState

class HelloWorldEpoxyFragment : BaseFragment() {
    private val viewModel by fragmentViewModel(HelloWorldViewModel::class)

    override fun EpoxyController.buildModels() = withState(viewModel) { state ->
        marquee {
            id("marquee")
            title(state.title)
        }
    }
}
