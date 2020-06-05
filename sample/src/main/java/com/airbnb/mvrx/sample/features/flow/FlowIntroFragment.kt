package com.airbnb.mvrx.sample.features.flow

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.sample.R
import com.airbnb.mvrx.sample.core.BaseFragment
import com.airbnb.mvrx.sample.databinding.FlowIntroFragmentBinding
import com.airbnb.mvrx.sample.utils.viewBinding
import com.airbnb.mvrx.sample.views.basicRow
import com.airbnb.mvrx.sample.views.marquee

class FlowIntroFragment : BaseFragment(R.layout.flow_intro_fragment) {
    private val binding: FlowIntroFragmentBinding by viewBinding()
    private val viewModel: FlowViewModel by activityViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setupWithNavController(findNavController())
        binding.recyclerView.withModels {
            marquee {
                id("marquee")
                title("Intro")
                subtitle("Set the initial counter value")
            }

            arrayOf(0, 10, 50, 100, 1_000, 10_000).forEach { count ->
                basicRow {
                    id(count)
                    title("$count")
                    clickListener { _ ->
                        viewModel.setCount(count)
                        findNavController().navigate(R.id.action_flowIntroFragment_to_flowCounterFragment)
                    }
                }
            }
        }
    }

    override fun invalidate() {
        // Do nothing.
    }
}
