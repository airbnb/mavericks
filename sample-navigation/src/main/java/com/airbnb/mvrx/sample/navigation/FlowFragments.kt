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
import com.airbnb.mvrx.withState
import kotlinx.android.synthetic.main.fragment_a.counterText
import kotlinx.android.synthetic.main.fragment_a.navigateButton

data class FlowNavigationState(@PersistState val count: Int = 0) : MavericksState

class FlowNavigationViewModel(state: FlowNavigationState) : MavericksViewModel<FlowNavigationState>(state) {
    fun incrementCount() = setState { copy(count = count + 1) }
}

class FlowFragmentA : Fragment(R.layout.fragment_a), MavericksView {
    /** This ViewModel will be shared across FlowFragmentA and FlowFragmentB. */
    private val viewModel: FlowNavigationViewModel by navGraphViewModel(R.id.nav_graph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        counterText.setOnClickListener {
            viewModel.incrementCount()
        }
        navigateButton.setOnClickListener {
            findNavController().navigate(R.id.action_fragment_a_to_fragment_b)
        }
    }

    override fun invalidate() = withState(viewModel) { state ->
        counterText.text = "${state.count}"
    }
}

class FlowFragmentB : Fragment(R.layout.fragment_b), MavericksView {
    /** This ViewModel will be shared across FlowFragmentA and FlowFragmentB. */
    private val viewModel: FlowNavigationViewModel by navGraphViewModel(R.id.nav_graph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        counterText.setOnClickListener {
            viewModel.incrementCount()
        }
    }

    override fun invalidate() = withState(viewModel) { state ->
        counterText.text = "${state.count}"
    }
}
