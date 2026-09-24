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
 *
 * [set] may be called from several threads at once, for example when a test's coroutines resume on
 * the threads of a real database or network layer. Each update is applied under a lock, so that no
 * reducer runs on a stale state and overwrites a concurrent update.
 */
@InternalMavericksApi
class SynchronousMavericksStateStore<S : Any>(initialState: S) : MavericksStateStore<S> {

    private val lock = Any()

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
        // The emit is inside the lock too, so the flow receives states in the order they were set
        // and its latest value is always the current state.
        synchronized(lock) {
            state = state.stateReducer()
            stateSharedFlow.tryEmit(state)
        }
    }
}
