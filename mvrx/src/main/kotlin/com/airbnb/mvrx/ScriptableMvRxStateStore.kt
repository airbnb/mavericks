package com.airbnb.mvrx

import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

/**
 * A [MvRxStateStore] which ignores standard calls to [set]. Instead it can be scripted via calls to
 * [next]. This is intended to be used for tests only, and in particular UI tests where you wish to test
 * how your UI code reacts to different ViewModel states. This is not as useful for unit testing your view model,
 * as business logic in state reducers will not be used.
 */
@Suppress("EXPERIMENTAL_API_USAGE")
class ScriptableMvRxStateStore<S : Any>(initialState: S) : MvRxStateStore<S> {

    private val channel = ConflatedBroadcastChannel<S>(initialState)

    override val flow: Flow<S> get() = channel.asFlow()

    override val state: S
        get() = channel.value

    override fun get(block: (S) -> Unit) {
        block(state)
    }

    override fun set(stateReducer: S.() -> S) {
        // No-op set the state via next
    }

    fun next(state: S) = channel.offer(state)
}