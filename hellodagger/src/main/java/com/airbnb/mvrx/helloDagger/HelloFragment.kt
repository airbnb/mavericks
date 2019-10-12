package com.airbnb.mvrx.helloDagger

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airbnb.mvrx.*
import kotlinx.android.synthetic.main.fragment_hello.*

class HelloFragment: BaseMvRxFragment() {

    private val viewModel: HelloViewModel by fragmentViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_hello, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        helloButton.setOnClickListener { viewModel.sayHello() }
    }

    override fun invalidate() {
        withState(viewModel) { state: HelloState ->
            when (state.message) {
                is Uninitialized, is Loading -> {
                    messageTextView.text = getString(R.string.helloFragmentLoadingText)
                    helloButton.isEnabled = false
                }
                is Success -> {
                    messageTextView.text = state.message()
                    helloButton.isEnabled = true
                }
                is Fail -> {
                    messageTextView.text = getString(R.string.helloFragmentFailureText)
                    helloButton.isEnabled = true
                }
            }
        }
    }
}