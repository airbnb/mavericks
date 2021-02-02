package com.airbnb.mvrx.hellohilt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.MvRxView
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.hellohilt.databinding.HelloHiltFragmentBinding
import com.airbnb.mvrx.withState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HelloHiltFragment : Fragment(R.layout.hello_hilt_fragment), MvRxView {
    val viewModel1: HelloHiltViewModel by fragmentViewModel(keyFactory = { "a" })
    val viewModel2: HelloHiltViewModel by fragmentViewModel(keyFactory = { "b" })

    private var _binding: HelloHiltFragmentBinding? = null
    private val binding get() = _binding ?: error("Binding was null!")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = HelloHiltFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun invalidate() = withState(viewModel1, viewModel2) { state1, state2 ->
        @Suppress("Detekt.MaxLineLength")
        binding.with.text = "@MavericksViewModelScoped: VM1: [${state1.viewModelScopedClassId1},${state1.viewModelScopedClassId2}] VM2: [${state2.viewModelScopedClassId1},${state2.viewModelScopedClassId2}]"
        @Suppress("Detekt.MaxLineLength")
        binding.without.text = "VM1: [${state1.notViewModelScopedClassId1},${state1.notViewModelScopedClassId2}] VM2: [${state2.notViewModelScopedClassId1},${state2.notViewModelScopedClassId2}]"
    }
}
