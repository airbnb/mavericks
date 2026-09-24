package com.airbnb.mvrx.mocking

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

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

    @Test
    fun concurrentSetsAreNotLost() {
        val store = SynchronousMavericksStateStore(TestState(num = 0))

        runConcurrently(THREAD_COUNT) {
            repeat(SETS_PER_THREAD) { store.set { copy(num = num + 1) } }
        }

        assertEquals(THREAD_COUNT * SETS_PER_THREAD, store.state.num)
    }

    @Test
    fun flowEndsOnLatestStateAfterConcurrentSets() = runBlocking {
        val store = SynchronousMavericksStateStore(TestState(num = 0))

        runConcurrently(THREAD_COUNT) {
            repeat(SETS_PER_THREAD) { store.set { copy(num = num + 1) } }
        }

        assertEquals(TestState(THREAD_COUNT * SETS_PER_THREAD), store.flow.firstOrNull())
    }

    private data class TestState(
        val num: Int = 1
    )

    private companion object {
        const val THREAD_COUNT = 8
        const val SETS_PER_THREAD = 5_000
    }
}

/** Runs [block] on [threadCount] threads, released together, and waits for all of them to finish. */
internal fun runConcurrently(threadCount: Int, block: () -> Unit) {
    val start = CountDownLatch(1)
    val threads = List(threadCount) {
        thread {
            start.await()
            block()
        }
    }
    start.countDown()
    threads.forEach { it.join() }
}
