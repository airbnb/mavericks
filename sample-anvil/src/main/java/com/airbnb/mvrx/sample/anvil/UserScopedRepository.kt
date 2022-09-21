package com.airbnb.mvrx.sample.anvil

import com.airbnb.mvrx.sample.anvil.di.SingleIn
import kotlinx.coroutines.delay
import javax.inject.Inject

@SingleIn(UserScope::class)
class UserScopedRepository @Inject constructor(
    private val user: User,
) {

    suspend fun helloWorld(): String {
        delay(2000)
        return "Hello World, ${user.name}!"
    }
}
