package com.airbnb.mvrx.sample.features.helloworld

import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.sample.core.MvRxViewModel

data class HelloWorldState(val title: String = "Hello World") : MvRxState

class HelloWorldViewModel(initialState: HelloWorldState) : MvRxViewModel<HelloWorldState>(initialState)
