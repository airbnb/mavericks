package com.airbnb.mvrx.hellodagger

import com.airbnb.mvrx.hellodagger.di.SingleIn
import com.gpeal.droidconanvilsample.lib.daggerscopes.AppScope
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
