package com.airbnb.mvrx.hellodagger

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HelloRepository @Inject constructor() {

    fun sayHello() = flowOf("Hello").map { value ->
        delay(2_000)
        value
    }
}
