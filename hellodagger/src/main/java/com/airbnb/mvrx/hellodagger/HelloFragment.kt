package com.airbnb.mvrx.hellodagger

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.mocking.MavericksViewMocks
import com.airbnb.mvrx.mocking.MockableMavericksView
import com.airbnb.mvrx.mocking.mockSingleViewModel
import com.airbnb.mvrx.withState
import kotlinx.android.synthetic.main.fragment_hello.helloButton
import kotlinx.android.synthetic.main.fragment_hello.messageTextView

class HelloFragment : Fragment(R.layout.fragment_hello), MockableMavericksView {

    val viewModel: HelloDaggerViewModel by fragmentViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        helloButton.setOnClickListener { viewModel.sayHello() }
    }

    override fun invalidate() = withState(viewModel) { state ->
        helloButton.isEnabled = state.message !is Loading
        messageTextView.text = when (state.message) {
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
        state("successfully") {
            copy(message = Success("I haz loaded"))
        }
    }
}
