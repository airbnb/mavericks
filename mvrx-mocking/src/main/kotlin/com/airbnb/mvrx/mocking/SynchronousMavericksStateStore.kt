package com.airbnb.mvrx.mocking

import androidx.annotation.RestrictTo
import com.airbnb.mvrx.MavericksStateStore
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * This acts as a functional state store, but all updates happen synchronously.
 * The intention of this is to allow state changes in tests to be tracked
 * synchronously.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class SynchronousMavericksStateStore<S : Any>(initialState: S) : MavericksStateStore<S> {

    private val stateSharedFlow = MutableSharedFlow<S>(
        replay = 1,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.SUSPEND,
    ).apply { tryEmit(initialState) }

    override val flow: Flow<S> = stateSharedFlow.asSharedFlow()

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
