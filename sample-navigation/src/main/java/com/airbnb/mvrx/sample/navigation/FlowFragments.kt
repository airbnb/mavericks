package com.airbnb.mvrx.sample.navigation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.PersistState
import com.airbnb.mvrx.navigation.navGraphViewModel
import com.airbnb.mvrx.sample.navigation.databinding.FragmentABinding
import com.airbnb.mvrx.sample.navigation.databinding.FragmentBBinding
import com.airbnb.mvrx.viewbinding.viewBinding
import com.airbnb.mvrx.withState

data class FlowNavigationState(@PersistState val count: Int = 0) : MavericksState

class FlowNavigationViewModel(state: FlowNavigationState) : MavericksViewModel<FlowNavigationState>(state) {
    fun incrementCount() = setState { copy(count = count + 1) }
}

class FlowFragmentA : Fragment(R.layout.fragment_a), MavericksView {
    private val binding: FragmentABinding by viewBinding()
    /** This ViewModel will be shared across FlowFragmentA and FlowFragmentB. */
    private val viewModel: FlowNavigationViewModel by navGraphViewModel(R.id.nav_graph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.counterText.setOnClickListener {
            viewModel.incrementCount()
        }
        binding.navigateButton.setOnClickListener {
            findNavController().navigate(R.id.action_fragment_a_to_fragment_b)
        }
    }

    override fun invalidate() = withState(viewModel) { state ->
        binding.counterText.text = "${state.count}"
    }
}

class FlowFragmentB : Fragment(R.layout.fragment_b), MavericksView {
    private val binding: FragmentBBinding by viewBinding()
    /** This ViewModel will be shared across FlowFragmentA and FlowFragmentB. */
    private val viewModel: FlowNavigationViewModel by navGraphViewModel(R.id.nav_graph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.counterText.setOnClickListener {
            viewModel.incrementCount()
        }
    }

    override fun invalidate() = withState(viewModel) { state ->
        binding.counterText.text = "${state.count}"
    }
}
