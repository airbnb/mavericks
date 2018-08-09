package com.airbnb.mvrx.sample.features.flow

import androidx.navigation.fragment.findNavController
import com.airbnb.epoxy.EpoxyController
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.sample.R
import com.airbnb.mvrx.sample.core.BaseFragment
import com.airbnb.mvrx.sample.views.basicRow
import com.airbnb.mvrx.sample.views.marquee
import com.airbnb.mvrx.withState

class FlowIntroFragment : BaseFragment() {

    private val viewModel by activityViewModel(FlowViewModel::class)

    override fun EpoxyController.buildModels() = withState(viewModel) { state ->
        marquee {
            id("marquee")
            title("Intro")
            subtitle("Set the initial counter value")
        }

        arrayOf(0, 10, 50, 100, 1_000, 10_000).forEach {
            basicRow {
                id(it)
                title("$it")
                clickListener { _ ->
                    viewModel.setCount(it)
                    findNavController().navigate(R.id.action_flowIntroFragment_to_flowCounterFragment)
                }
            }
        }
    }
}