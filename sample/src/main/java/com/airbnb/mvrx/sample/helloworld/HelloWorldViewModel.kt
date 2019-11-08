package com.airbnb.mvrx.sample.helloworld

import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.sample.core.app.MvRxViewModel

data class HelloWorldState(val title: HelloWorld = HelloWorld()) : MvRxState

// This is done to have a non primitive type element in the state object
data class HelloWorld (val value: String = "Hello World")

class HelloWorldViewModel(initialState: HelloWorldState) : MvRxViewModel<HelloWorldState>(initialState)
