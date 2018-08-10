package com.airbnb.mvrx.sample.features.flow

import com.airbnb.epoxy.EpoxyController
import com.airbnb.mvrx.existingViewModel
import com.airbnb.mvrx.sample.core.BaseFragment
import com.airbnb.mvrx.sample.views.marquee
import com.airbnb.mvrx.withState

class FlowCounterFragment : BaseFragment() {
    /**
     * Because we know that this Fragment isn't the first Fragment in the flow and that the ViewModel
     * will have been created by an earlier screen in the Fragment, we can use [existingViewModel]
     * which will throw an exception if it hasn't been created yet.
     *
     * This is useful if your ViewModel has dependencies for its initial state or anything else.
     */
    private val viewModel by existingViewModel(FlowViewModel::class)

    override fun EpoxyController.buildModels() = withState(viewModel) { state ->
        marquee {
            id("marquee")
            title("Count: ${state.count}")
            subtitle("Not persisted counter: ${state.notPersistedCount}")
        }
    }
}