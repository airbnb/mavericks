package com.airbnb.mvrx.helloDagger

import android.os.Bundle
import android.view.View
import com.airbnb.mvrx.*
import kotlinx.android.synthetic.main.fragment_hello.*

class HelloFragment: BaseMvRxFragment(R.layout.fragment_hello) {

    val viewModel: HelloViewModel by fragmentViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        helloButton.setOnClickListener { viewModel.sayHello() }
    }

    override fun invalidate() = withState(viewModel) { state ->
        helloButton.isEnabled = state.message !is Loading
        messageTextView.text = when (state.message) {
            is Uninitialized, is Loading ->  getString(R.string.hello_fragment_loading_text)
            is Success -> state.message()
            is Fail ->  getString(R.string.hello_fragment_failure_text)
        }
    }
}