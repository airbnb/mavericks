package com.airbnb.mvrx.counter

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.PersistState
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.counter.databinding.CounterFragmentBinding
import com.airbnb.mvrx.viewbinding.viewBinding
import com.airbnb.mvrx.withState

data class CounterState(@PersistState val count: Int = 0) : MavericksState

class CounterViewModel(state: CounterState) : MavericksViewModel<CounterState>(state) {

    fun incrementCount() = setState { copy(count = count + 1) }
}

class CounterFragment : Fragment(R.layout.counter_fragment), MavericksView {
    private val binding: CounterFragmentBinding by viewBinding()
    private val viewModel: CounterViewModel by activityViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.counterText.setOnClickListener {
            viewModel.incrementCount()
        }
    }

    override fun invalidate() = withState(viewModel) { state ->
        binding.counterText.text = "${state.count}"
    }
}
