package com.airbnb.mvrx

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

data class MvRxStateStoreTestState(val count: Int = 1, val list: List<Int> = emptyList()) : MvRxState

@Suppress("EXPERIMENTAL_API_USAGE")
class StateStoreTest : BaseTest() {

    @Test
    fun testGetRunsSynchronouslyForTests() {
        val store = CoroutinesStateStore(MvRxStateStoreTestState())
        var callCount = 0
        store.get { callCount++ }
        assertEquals(1, callCount)
    }

    @Test
    fun testSetState() {
        val store = CoroutinesStateStore(MvRxStateStoreTestState())
        store.set {
            copy(count = 2)
        }
        var called = false
        store.get {
            assertEquals(2, it.count)
            called = true
        }
        assertTrue(called)
    }

    @Test
    fun testSubscribeNotCalledForNoop() {
        val scope = TestCoroutineScope()
        val store = CoroutinesStateStore(MvRxStateStoreTestState())
        var callCount = 0
        store.flow.onEach {
            callCount++
        }.launchIn(scope)
        assertEquals(1, callCount)
        store.set { this }
        assertEquals(1, callCount)
    }

    @Test
    fun testSubscribeNotCalledForSameValue() {
        val scope = TestCoroutineScope()
        val store = CoroutinesStateStore(MvRxStateStoreTestState())
        var callCount = 0
        store.flow.onEach {
            callCount++
        }.launchIn(scope)
        assertEquals(1, callCount)
        store.set { copy() }
        assertEquals(1, callCount)
    }

    @Test
    fun testScope() = runBlockingTest {
        val store = CoroutinesStateStore(MvRxStateStoreTestState(), this)
        val job = store.flow.onEach {
            println("it")
        }.launchIn(this)
        job.cancel()
    }
}
