package com.airbnb.mvrx.sample.anvil

import com.airbnb.mvrx.sample.anvil.di.SingleIn
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.random.Random

class DelayProvider @Inject constructor() {
    operator fun invoke() = Random.nextLong(from = 1000L, until = 2000L)
}

@SingleIn(UserScope::class)
class UserScopedRepository @Inject constructor(
    private val delayProvider: DelayProvider,
    private val user: User,
) {

    suspend fun helloWorld(): String {
        delay(delayProvider())
        return "Hello World, ${user.name}!"
    }
}
