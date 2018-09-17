package com.airbnb.mvrx

import io.reactivex.Observable

interface StateStore<S : Any> {

    val observable: Observable<S>
    /**
     * This is automatically updated from a subscription on the subject for easy access to the
     * current state.
     */
    val state: S
        // value must be present here, since the subject is created with initialState

    /**
     * Get the current state. The block of code is posted to a queue and all pending setState blocks
     * are guaranteed to run before the get block is run.
     */
    fun get(block: (S) -> Unit)

    /**
     * Call this to update the state. The state reducer will get added to a queue that is processes
     * on a background thread. The state reducer's receiver type is the current state when the
     * reducer is called.
     *
     * An example of a reducer would be `{ copy(myProperty = 5) }`. The copy comes from the copy
     * function on a Kotlin data class and can be called directly because state is the receiver type
     * of the reducer. In this case, it will also implicitly return the only expression so that is
     * all of the code required.
     */
    fun set(stateReducer: S.() -> S)
}