package com.airbnb.mvrx.sample.features.helloworld

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.sample.R
import com.airbnb.mvrx.sample.core.BaseFragment
import com.airbnb.mvrx.sample.core.MvRxViewModel
import com.airbnb.mvrx.sample.databinding.HelloWorldFragmentBinding
import com.airbnb.mvrx.sample.utils.viewBinding
import com.airbnb.mvrx.withState

data class HelloWorldState(val title: String = "Hello World") : MavericksState

class HelloWorldViewModel(initialState: HelloWorldState) : MvRxViewModel<HelloWorldState>(initialState)

class HelloWorldFragment : BaseFragment(R.layout.hello_world_fragment) {
    private val binding: HelloWorldFragmentBinding by viewBinding()
    private val viewModel: HelloWorldViewModel by fragmentViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setupWithNavController(findNavController())
    }

    override fun invalidate() = withState(viewModel) { state ->
        binding.marquee.setTitle(state.title)
    }
}
