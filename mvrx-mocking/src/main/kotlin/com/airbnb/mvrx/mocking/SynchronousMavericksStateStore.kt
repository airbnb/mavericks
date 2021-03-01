package com.airbnb.mvrx.mocking

import com.airbnb.mvrx.InternalMavericksApi
import com.airbnb.mvrx.MavericksStateStore
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * This acts as a functional state store, but all updates happen synchronously.
 * The intention of this is to allow state changes in tests to be tracked
 * synchronously.
 */
@InternalMavericksApi
class SynchronousMavericksStateStore<S : Any>(initialState: S) : MavericksStateStore<S> {

    private val stateSharedFlow = MutableSharedFlow<S>(
        replay = 1,
        extraBufferCapacity = 63,
        onBufferOverflow = BufferOverflow.SUSPEND,
    ).apply { tryEmit(initialState) }

    override val flow: Flow<S> = stateSharedFlow.asSharedFlow().distinctUntilChanged()

    @Volatile
    override var state: S = initialState
        private set

    override fun get(block: (S) -> Unit) {
        block(state)
    }

    override fun set(stateReducer: S.() -> S) {
        state = state.stateReducer()
        stateSharedFlow.tryEmit(state)
    }
}
