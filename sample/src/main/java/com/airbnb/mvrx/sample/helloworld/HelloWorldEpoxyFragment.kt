package com.airbnb.mvrx.sample.helloworld

import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.sample.core.app.BaseFragment
import com.airbnb.mvrx.sample.core.app.simpleController
import com.airbnb.mvrx.sample.core.views.marquee

class HelloWorldEpoxyFragment : BaseFragment() {
    private val viewModel: HelloWorldViewModel by fragmentViewModel()

    override fun epoxyController() = simpleController(viewModel) { state ->
        marquee {
            id("marquee")
            title(state.title.value)
        }
    }
}
