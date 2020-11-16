package com.airbnb.mvrx.mocking

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

// Not extending BaseTest to avoid making Rx synchronous, since we want to test that this
// state store is synchronous by default.
class SynchronousMavericksStateStoreTest {
    @Test
    fun setAndGetStateSynchronously() {
        val store = SynchronousMavericksStateStore(TestState())
        store.set { TestState(5) }
        store.get { state ->
            assertEquals(5, state.num)
        }
    }

    @Test
    fun flowWorks() = runBlocking {
        val store = SynchronousMavericksStateStore(TestState())
        store.set { TestState(5) }

        val flowResult = store.flow.firstOrNull()
        assertEquals(TestState(5), flowResult)
    }

    private data class TestState(
        val num: Int = 1
    )
}
