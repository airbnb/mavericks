package com.airbnb.mvrx

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * A [MavericksStateStore] which ignores standard calls to [set]. Instead it can be scripted via calls to
 * [next]. This is intended to be used for tests only, and in particular UI tests where you wish to test
 * how your UI code reacts to different ViewModel states. This is not as useful for unit testing your view model,
 * as business logic in state reducers will not be used.
 */
@Suppress("EXPERIMENTAL_API_USAGE")
class ScriptableMavericksStateStore<S : Any>(initialState: S) : ScriptableStateStore<S> {

    private val stateSharedFlow = MutableSharedFlow<S>(
        replay = 1,
        extraBufferCapacity = CoroutinesStateStore.SubscriberBufferSize,
        onBufferOverflow = BufferOverflow.SUSPEND,
    ).apply { tryEmit(initialState) }

    @Volatile
    override var state = initialState

    override val flow: Flow<S> = stateSharedFlow.asSharedFlow().distinctUntilChanged()

    override fun get(block: (S) -> Unit) {
        block(state)
    }

    override fun set(stateReducer: S.() -> S) {
        // No-op set the state via next
    }

    override fun next(state: S) {
        this.state = state
        stateSharedFlow.tryEmit(state)
    }
}

interface ScriptableStateStore<S : Any> : MavericksStateStore<S> {
    /** Force the current state to be moved to the given value immediately. */
    fun next(state: S)
}
