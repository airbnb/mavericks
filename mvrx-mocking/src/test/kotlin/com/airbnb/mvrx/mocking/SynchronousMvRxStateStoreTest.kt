package com.airbnb.mvrx.mocking

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.junit.Test

// Not extending BaseTest to avoid making Rx synchronous, since we want to test that this
// state store is synchronous by default.
class SynchronousMvRxStateStoreTest {
    @Test
    fun setAndGetStateSynchronously() {
        val store = SynchronousMvRxStateStore(TestState(), testCoroutineScope())
        store.set { TestState(5) }
        store.get { state ->
            assertEquals(5, state.num)
        }
    }

    @Test
    fun flowWorks() = runBlocking {
        val store = SynchronousMvRxStateStore(TestState(), testCoroutineScope())
        store.set { TestState(5) }

        val flowResult = store.flow.firstOrNull()
        assertEquals(TestState(5), flowResult)
    }

    private data class TestState(
        val num: Int = 1
    )
}
