package com.airbnb.mvrx.hellodagger

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.hellodagger.databinding.HelloFragmentBinding
import com.airbnb.mvrx.mocking.MavericksViewMocks
import com.airbnb.mvrx.mocking.MockableMavericksView
import com.airbnb.mvrx.mocking.mockSingleViewModel
import com.airbnb.mvrx.viewbinding.viewBinding
import com.airbnb.mvrx.withState

class HelloFragment : Fragment(R.layout.hello_fragment), MockableMavericksView {
    private val binding: HelloFragmentBinding by viewBinding()

    val viewModel: HelloDaggerViewModel by fragmentViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.helloButton.setOnClickListener { viewModel.sayHello() }
    }

    override fun invalidate() = withState(viewModel) { state ->
        binding.helloButton.isEnabled = state.message !is Loading
        binding.messageTextView.text = when (state.message) {
            is Uninitialized, is Loading -> getString(R.string.hello_fragment_loading_text)
            is Success -> state.message()
            is Fail -> getString(R.string.hello_fragment_failure_text)
        }
    }

    override fun provideMocks(): MavericksViewMocks<out MockableMavericksView, out Parcelable> = mockSingleViewModel(
        viewModelReference = HelloFragment::viewModel,
        defaultState = HelloDaggerState(),
        defaultArgs = null
    ) {
        state("Success") {
            copy(message = Success("Hello World"))
        }
    }
}
