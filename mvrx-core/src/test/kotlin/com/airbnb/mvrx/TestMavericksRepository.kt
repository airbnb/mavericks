package com.airbnb.mvrx

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

abstract class TestMavericksRepository<S : MavericksState>(initialState: S) : MavericksRepository<S>(
    initialState = initialState,
    coroutineScope = CoroutineScope(Dispatchers.Unconfined),
    performCorrectnessValidations = true,
)
