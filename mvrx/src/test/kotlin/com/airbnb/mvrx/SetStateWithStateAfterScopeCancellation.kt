package com.airbnb.mvrx

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import org.junit.Test

class SetStateWithStateAfterScopeCancellation : BaseTest() {
    data class State(val foo: Int) : MvRxState

    @Test
    fun setStateAfterScopeCancellation() {
        val scope = CoroutineScope(Dispatchers.Default + Job())
        scope.cancel()
        val store = CoroutinesStateStore(State(foo = 0), scope)
        store.set { copy(foo = foo + 1) }
    }

    @Test
    fun withStateAfterScopeCancellation() {
        val scope = CoroutineScope(Dispatchers.Default + Job())
        scope.cancel()
        val store = CoroutinesStateStore(State(foo = 0), scope)
        store.get {  }
    }

}