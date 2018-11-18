package com.airbnb.mvrx.sample.features.helloworld

import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.sample.core.MvRxViewModel
import javax.inject.Inject

data class HelloWorldState(val title: String = "Hello World") : MvRxState

class HelloWorldViewModel @Inject constructor(): MvRxViewModel<HelloWorldState>(HelloWorldState())
