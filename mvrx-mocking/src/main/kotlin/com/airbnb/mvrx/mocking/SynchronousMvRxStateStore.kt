package com.airbnb.mvrx.mocking

import androidx.annotation.RestrictTo
import com.airbnb.mvrx.MvRxStateStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow

/**
 * This acts as a functional state store, but all updates happen synchronously.
 * The intention of this is to allow state changes in tests to be tracked
 * synchronously.
 */
@Suppress("EXPERIMENTAL_API_USAGE")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class SynchronousMvRxStateStore<S : Any>(initialState: S, coroutineScope: CoroutineScope) : MvRxStateStore<S> {

    private val stateChannel = BroadcastChannel<S>(capacity = Channel.BUFFERED)

    init {
        coroutineScope.coroutineContext[Job]!!.invokeOnCompletion {
            stateChannel.cancel()
        }
    }

    @Volatile
    override var state: S = initialState
        private set

    override fun get(block: (S) -> Unit) {
        block(state)
    }

    override fun set(stateReducer: S.() -> S) {
        state = state.stateReducer()
        stateChannel.sendBlocking(state)
    }

    override val flow: Flow<S>
        get() = flow {
            emit(state)
            stateChannel.consumeEach { emit(it) }
        }.buffer(1).distinctUntilChanged()
}
