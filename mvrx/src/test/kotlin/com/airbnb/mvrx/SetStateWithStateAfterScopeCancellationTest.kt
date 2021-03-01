package com.airbnb.mvrx

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import org.junit.Assert.assertEquals
import org.junit.Test

class SetStateWithStateAfterScopeCancellationTest : BaseTest() {
    data class State(val foo: Int) : MavericksState

    @Test
    fun setStateAfterScopeCancellation() {
        val scope = CoroutineScope(Dispatchers.Default + Job())
        scope.cancel()
        val store = CoroutinesStateStore(State(foo = 0), scope)
        store.set { copy(foo = foo + 1) }
        // ensure set operation above is ignored
        assertEquals(0, store.state.foo)
    }

    @Test
    fun withStateAfterScopeCancellation() {
        val scope = CoroutineScope(Dispatchers.Default + Job())
        scope.cancel()
        val store = CoroutinesStateStore(State(foo = 0), scope)
        store.get { }
    }
}