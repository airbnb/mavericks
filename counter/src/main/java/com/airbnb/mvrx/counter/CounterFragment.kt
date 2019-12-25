package com.airbnb.mvrx.counter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.PersistState
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.withState
import kotlinx.android.synthetic.main.fragment_counter.counterText

data class CounterState(@PersistState val count: Int = 0) : MvRxState

class CounterViewModel(state: CounterState) : MvRxViewModel<CounterState>(state) {

    fun incrementCount() = setState { copy(count = count + 1) }
}

class CounterFragment : BaseMvRxFragment() {

    private val viewModel: CounterViewModel by activityViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_counter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        counterText.setOnClickListener {
            viewModel.incrementCount()
        }
    }

    override fun invalidate() = withState(viewModel) { state ->
        counterText.text = "${state.count}"
    }
}
