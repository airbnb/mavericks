package com.airbnb.mvrx

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow

/**
 * A [MvRxStateStore] which ignores standard calls to [set]. Instead it can be scripted via calls to
 * [next]. This is intended to be used for tests only, and in particular UI tests where you wish to test
 * how your UI code reacts to different ViewModel states. This is not as useful for unit testing your view model,
 * as business logic in state reducers will not be used.
 */
@Suppress("EXPERIMENTAL_API_USAGE")
class ScriptableMvRxStateStore<S : Any>(initialState: S) : MvRxStateStore<S> {

    private val stateChannel = BroadcastChannel<S>(capacity = Channel.BUFFERED)
    override var state = initialState
    override val flow: Flow<S>
        get() = flow {
            emit(state)
            stateChannel.consumeEach { emit(it) }
        }.buffer(1).distinctUntilChanged()

    override fun get(block: (S) -> Unit) {
        block(state)
    }

    override fun set(stateReducer: S.() -> S) {
        // No-op set the state via next
    }

    fun next(state: S) {
        this.state = state
        stateChannel.offer(state)
    }
}