package com.airbnb.mvrx

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.EmptyCoroutineContext

abstract class TestMavericksRepository<S : MavericksState>(initialState: S) : MavericksRepository<S>(
    initialState = initialState,
    configProvider = {
        MavericksRepositoryConfig(
            debugMode = true,
            stateStore = CoroutinesStateStore(initialState, CoroutineScope(Dispatchers.Unconfined)),
            coroutineScope = CoroutineScope(Dispatchers.Unconfined),
            subscriptionCoroutineContextOverride = EmptyCoroutineContext,
            onExecute = { MavericksBlockExecutions.No }
        )
    }
)
