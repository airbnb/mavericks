package com.airbnb.mvrx.sample.features.parentfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.parentFragmentViewModel
import com.airbnb.mvrx.sample.R
import com.airbnb.mvrx.withState
import kotlinx.android.synthetic.main.fragment_parent.textView

class ChildFragment : BaseMvRxFragment() {

    private val viewModel: CounterViewModel by parentFragmentViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_child, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        textView.setOnClickListener {
            viewModel.incrementCount()
        }
    }

    override fun invalidate() = withState(viewModel) { state ->
        textView.text = "ChildFragment: Count: ${state.count}"
    }
}