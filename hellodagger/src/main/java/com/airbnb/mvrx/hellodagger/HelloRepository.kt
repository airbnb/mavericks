package com.airbnb.mvrx.hellodagger

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class HelloRepository @Inject constructor() {

    fun sayHello() = flow {
        delay(2_000)
        emit("Hello")
    }
}
