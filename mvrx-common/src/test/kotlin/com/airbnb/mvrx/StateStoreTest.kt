package com.airbnb.mvrx

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

data class MavericksStateStoreTestState(val count: Int = 1, val list: List<Int> = emptyList()) : MavericksState

@Suppress("EXPERIMENTAL_API_USAGE")
class StateStoreTest : BaseTest() {

    @Test
    fun testGetRunsSynchronouslyForTests() = runBlocking {
        val store = CoroutinesStateStore(MavericksStateStoreTestState(), this)
        var callCount = 0
        store.get { callCount++ }
        assertEquals(1, callCount)
    }

    @Test
    fun testSetState() = runBlocking {
        val store = CoroutinesStateStore(MavericksStateStoreTestState(), this)
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
    fun testSubscribeNotCalledForNoop() = runTest(UnconfinedTestDispatcher()) {
        val store = CoroutinesStateStore(MavericksStateStoreTestState(), this)
        var callCount = 0
        val job = store.flow.onEach {
            callCount++
        }.launchIn(this)
        assertEquals(1, callCount)
        store.set { this }
        assertEquals(1, callCount)
        job.cancel()
    }

    @Test
    fun testSubscribeNotCalledForSameValue() = runTest(UnconfinedTestDispatcher()) {
        val store = CoroutinesStateStore(MavericksStateStoreTestState(), this)
        var callCount = 0
        val job = store.flow.onEach {
            callCount++
        }.launchIn(this)
        assertEquals(1, callCount)
        store.set { copy() }
        assertEquals(1, callCount)
        job.cancel()
    }

    @Test
    fun testBlockingReceiver() = runTest(UnconfinedTestDispatcher()) {
        val store = CoroutinesStateStore(MavericksStateStoreTestState(), this)
        val values = mutableListOf<Int>()
        val job1 = launch {
            store.flow.collect {
                values += it.count
                delay(10)
            }
        }

        (2..10).forEach {
            store.set { copy(count = it) }
        }
        delay(100)
        job1.cancel()
        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), values)
    }

    @Test(timeout = 5_000)
    fun testConcurrentSetWhenForceSynchronousDoesNotDeadlock() = runBlocking {
        val store = CoroutinesStateStore(MavericksStateStoreTestState(), this)
        val firstReducerEntered = java.util.concurrent.CountDownLatch(1)
        val secondThreadReady = java.util.concurrent.CountDownLatch(1)

        val thread = Thread {
            firstReducerEntered.await()
            store.set {
                copy(count = count + 1)
            }
        }
        thread.start()

        store.set {
            firstReducerEntered.countDown()
            // Give the second thread a brief moment to enqueue its reducer via trySend
            // before this first reducer returns.
            secondThreadReady.await(50, java.util.concurrent.TimeUnit.MILLISECONDS)
            copy(count = count + 1)
        }

        thread.join(2_000)
        assertEquals(3, store.state.count)
    }
}
