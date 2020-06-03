package com.airbnb.mvrx.sample.features.parentfragment

import android.os.Bundle
import android.view.View
import com.airbnb.mvrx.parentFragmentViewModel
import com.airbnb.mvrx.sample.R
import com.airbnb.mvrx.sample.core.BaseFragment
import com.airbnb.mvrx.sample.databinding.ChildFragmentBinding
import com.airbnb.mvrx.sample.utils.viewBinding
import com.airbnb.mvrx.withState

class ChildFragment : BaseFragment(R.layout.child_fragment) {
    private val binding: ChildFragmentBinding by viewBinding()
    private val viewModel: ParentChildSharedViewModel by parentFragmentViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.root.setOnClickListener {
            viewModel.incrementCount()
        }
    }

    override fun invalidate() = withState(viewModel) { state ->
        binding.text.text = "ChildFragment: Count: ${state.count}"
    }
}
