package com.airbnb.mvrx.sample.features.parentfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.sample.R
import com.airbnb.mvrx.sample.core.MvRxViewModel
import com.airbnb.mvrx.withState
import kotlinx.android.synthetic.main.fragment_parent.textView
import kotlinx.android.synthetic.main.fragment_parent.toolbar

data class CounterState(val count: Int = 0) : MvRxState
class CounterViewModel(state: CounterState) : MvRxViewModel<CounterState>(state) {
    fun incrementCount() = setState { copy(count = count + 1) }
}

class ParentFragment : BaseMvRxFragment() {

    private val viewModel: CounterViewModel by fragmentViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_parent, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        toolbar.setupWithNavController(findNavController())
        textView.setOnClickListener {
            viewModel.incrementCount()
        }
        childFragmentManager.beginTransaction()
            .replace(R.id.childContainer, ChildFragment())
            .commit()
    }

    override fun invalidate() = withState(viewModel) { state ->
        textView.text = "ParentFragment: Count: ${state.count}"
    }
}