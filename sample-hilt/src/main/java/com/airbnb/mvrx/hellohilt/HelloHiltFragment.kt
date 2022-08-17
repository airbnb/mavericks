package com.airbnb.mvrx.hellohilt

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.hellohilt.databinding.HelloHiltFragmentBinding
import com.airbnb.mvrx.viewbinding.viewBinding
import com.airbnb.mvrx.withState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HelloHiltFragment : Fragment(R.layout.hello_hilt_fragment), MavericksView {
    private val binding: HelloHiltFragmentBinding by viewBinding()
    private val viewModel1: HelloHiltViewModel by fragmentViewModel(keyFactory = { "a" })
    private val viewModel2: HelloHiltViewModel by fragmentViewModel(keyFactory = { "b" })

    @SuppressLint("SetTextI18n")
    override fun invalidate() = withState(viewModel1, viewModel2) { state1, state2 ->
        @Suppress("Detekt.MaxLineLength")
        binding.with.text =
            "@MavericksViewModelScoped: VM1: [${state1.viewModelScopedClassId1},${state1.viewModelScopedClassId2}] VM2: [${state2.viewModelScopedClassId1},${state2.viewModelScopedClassId2}]"
        @Suppress("Detekt.MaxLineLength")
        binding.without.text =
            "VM1: [${state1.notViewModelScopedClassId1},${state1.notViewModelScopedClassId2}] VM2: [${state2.notViewModelScopedClassId1},${state2.notViewModelScopedClassId2}]"
    }
}
