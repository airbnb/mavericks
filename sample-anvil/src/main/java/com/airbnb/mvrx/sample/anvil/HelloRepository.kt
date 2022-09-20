package com.airbnb.mvrx.sample.anvil

import com.airbnb.mvrx.anvil.AppScope
import com.airbnb.mvrx.sample.anvil.di.SingleIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import kotlin.random.Random

class DelayProvider @Inject constructor() {
    operator fun invoke() = Random.nextLong(from = 1000L, until = 2000L)
}

@SingleIn(AppScope::class)
class HelloRepository @Inject constructor(
    private val delayProvider: DelayProvider,
) {

    fun sayHello() = flow {
        delay(delayProvider())
        emit("Hello")
    }
}
