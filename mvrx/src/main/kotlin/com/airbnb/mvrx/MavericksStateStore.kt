package com.airbnb.mvrx

import androidx.annotation.RestrictTo
import kotlinx.coroutines.flow.Flow

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@InternalMavericksApi
interface MavericksStateStore<S : Any> {
    val state: S
    val flow: Flow<S>
    fun get(block: (S) -> Unit)
    fun set(stateReducer: S.() -> S)
}
