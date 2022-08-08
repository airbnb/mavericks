package com.airbnb.mvrx

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

abstract class BaseTestMavericksRepository<S : MavericksState>(initialState: S) : MavericksRepository<S>(
    initialState = initialState,
    coroutineScope = CoroutineScope(Dispatchers.Unconfined),
    performCorrectnessValidations = true,
) {
    fun tearDown() {
        coroutineScope.cancel()
    }
}
