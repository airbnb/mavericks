package com.airbnb.mvrx

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.EmptyCoroutineContext

class TestMavericksRepositoryConfig<S : MavericksState>(initialState: S) : MavericksRepositoryConfig<S>(
    debugMode = true,
    stateStore = CoroutinesStateStore(initialState, CoroutineScope(Dispatchers.Unconfined)),
    coroutineScope = CoroutineScope(Dispatchers.Unconfined),
    subscriptionCoroutineContextOverride = EmptyCoroutineContext,
) {
    override fun <S : MavericksState> onExecute(repository: MavericksRepository<S>): MavericksBlockExecutions {
        return MavericksBlockExecutions.No
    }
}