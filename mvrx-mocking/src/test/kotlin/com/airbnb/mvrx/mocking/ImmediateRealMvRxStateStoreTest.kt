package com.airbnb.mvrx.mocking

import org.junit.Assert.*
import org.junit.Test

// Not extending BaseTest to avoid making Rx synchronous, since we want to test that this
// state store is synchronous by default.
class ImmediateRealMvRxStateStoreTest {
    @Test
    fun setAndGetStateSynchronously() {
        val store = com.airbnb.mvrx.mocking.ImmediateRealMvRxStateStore(TestState())
        store.set { TestState(5) }
        store.get { state ->
            assertEquals(5, state.num)
        }
    }

    @Test
    fun observableWorks() {
        val store = com.airbnb.mvrx.mocking.ImmediateRealMvRxStateStore(TestState())
        store.set { TestState(5) }

        val testObserver = store.observable.firstOrError().test()
        testObserver.assertValue(TestState(5))
    }

    @Test
    fun dispose() {
        val store = com.airbnb.mvrx.mocking.ImmediateRealMvRxStateStore(TestState())
        store.dispose()
        assertTrue(store.isDisposed)
    }

    private data class TestState(
        val num: Int = 1
    )
}
