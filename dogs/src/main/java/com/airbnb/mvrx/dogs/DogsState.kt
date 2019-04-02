package com.airbnb.mvrx.dogs

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.Uninitialized

data class DogsState(val dogs: Async<List<Dog>> = Uninitialized) : MvRxState