package com.airbnb.mvrx.mocking

import com.airbnb.mvrx.MvRxStateStore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/**
 * This acts as a functional state store, but all updates happen synchronously.
 * The intention of this is to allow state changes in tests to be tracked
 * synchronously.
 */
internal class SynchronousMvRxStateStore<S : Any>(initialState: S) : MvRxStateStore<S> {

    private val flowEmitters = mutableListOf<suspend (S) -> Unit>()

    @Volatile
    override var state: S = initialState
        private set


    override fun get(block: (S) -> Unit) {
        block(state)
    }

    override fun set(stateReducer: S.() -> S) {
        state = state.stateReducer()
        // TODO: Neeed coroutineScope to emit to flow
        GlobalScope.launch {
            flowEmitters.forEach { it(state) }
        }
    }

    override val flow: Flow<S>
        get() {
            return flow {
                emit(state)
                flowEmitters.add { newState -> emit(newState) }
            }
        }
}
