package com.airbnb.mvrx.counter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.PersistState
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.withState
import kotlinx.android.synthetic.main.fragment_counter.counterText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class CounterState(@PersistState val count: Int = 0) : MvRxState

class CounterViewModel(state: CounterState) : MavericksViewModel<CounterState>(state) {

    private var queueSize = 0

    init {
        viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                incrementCount()
                delay(16)
            }
        }
    }

    fun incrementCount() {
        Log.d("Gabe", "CounterFragment#incrementCount: $queueSize")
        queueSize += 2
        setState {
            queueSize--
            copy(count = count + 1)
        }
    }
}

class CounterFragment : Fragment(), MavericksView {

    private val viewModel: CounterViewModel by activityViewModel()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_counter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        counterText.setOnClickListener {
            viewModel.incrementCount()
        }
    }

    override fun onStart() {
        super.onStart()
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.stateFlow.collect {
                Log.d("Gabe", "CounterFragment#onStart: sleeping")
                Thread.sleep(32)
            }
        }
    }

    override fun invalidate() = withState(viewModel) { state ->
        counterText.text = "${state.count}"
    }
}
