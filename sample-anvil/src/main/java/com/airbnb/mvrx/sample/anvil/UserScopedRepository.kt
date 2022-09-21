package com.airbnb.mvrx.sample.anvil

import com.airbnb.mvrx.sample.anvil.di.SingleIn
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * This doesn't need to be a singleton in the user component but is done just to demonstrate
 * how to create singletons with [SingleIn].
 */
@SingleIn(UserScope::class)
class UserScopedRepository @Inject constructor(
    private val user: User,
) {

    suspend operator fun invoke(): String {
        delay(2000)
        return "Hello World, ${user.name}!"
    }
}
