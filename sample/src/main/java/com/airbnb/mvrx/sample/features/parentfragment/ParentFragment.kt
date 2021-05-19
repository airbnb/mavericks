package com.airbnb.mvrx.sample.features.parentfragment

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.sample.R
import com.airbnb.mvrx.sample.core.BaseFragment
import com.airbnb.mvrx.sample.databinding.ParentFragmentBinding
import com.airbnb.mvrx.viewbinding.viewBinding
import com.airbnb.mvrx.withState

class ParentFragment : BaseFragment(R.layout.parent_fragment) {
    private val binding: ParentFragmentBinding by viewBinding()
    private val viewModel: ParentChildSharedViewModel by fragmentViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setupWithNavController(findNavController())
        binding.root.setOnClickListener {
            viewModel.incrementCount()
        }
    }

    override fun invalidate() = withState(viewModel) { state ->
        binding.textView.text = "ParentFragment: Count: ${state.count}"
    }
}
