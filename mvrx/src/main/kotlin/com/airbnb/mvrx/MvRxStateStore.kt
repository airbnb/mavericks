package com.airbnb.mvrx

import kotlinx.coroutines.flow.Flow

interface MvRxStateStore<S : Any> {
    val state: S
    val flow: Flow<S>
    fun get(block: (S) -> Unit)
    fun set(stateReducer: S.() -> S)
}
