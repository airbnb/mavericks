package com.airbnb.mvrx.hellokoin.screens.hello

import android.os.Bundle
import android.view.View
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Incomplete
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.hellokoin.R
import com.airbnb.mvrx.withState
import kotlinx.android.synthetic.main.fragment_hello.helloButton
import kotlinx.android.synthetic.main.fragment_hello.messageTextView

class HelloFragment : BaseMvRxFragment(R.layout.fragment_hello) {

    val viewModel: HelloViewModel by fragmentViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        helloButton.setOnClickListener { viewModel.sayHello() }
        // To immediately apply state from VM to UI
        invalidate()
    }

    override fun invalidate() = withState(viewModel) { state ->
        helloButton.isEnabled = state.message !is Incomplete
        messageTextView.text = when (state.message) {
            is Uninitialized, is Loading -> getString(R.string.hello_fragment_loading_text)
            is Success -> state.message()
            is Fail -> getString(R.string.hello_fragment_failure_text)
        }
    }
}