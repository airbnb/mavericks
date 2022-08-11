package com.airbnb.mvrx

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert
import org.junit.Test

class CoroutineStateStoreReplayTest {
    data class State(val foo: Int) : MavericksState

    @Test
    fun replayTest() = runBlocking {
        repeat(100) {
            singleReplayTestIteration(n = 5000, subscribers = 10)
        }
    }

    @Test
    fun replayLargeTest() = runBlocking {
        singleReplayTestIteration(n = 100_000, subscribers = 10)
    }

    /**
     * Tests consistency of produced flow. E.g. for just increment reducer output must be
     * 1,2,3,4,5
     * not 1,3,4,5 (value missing)
     * or 4,3,4,5 (incorrect order)
     * or 3,3,4,5 (duplicate value)
     */
    @Suppress("DeferredResultUnused")
    private suspend fun singleReplayTestIteration(n: Int, subscribers: Int) = withContext(Dispatchers.Default) {
        val scope = CoroutineScope(Dispatchers.Default + Job())
        val store = CoroutinesStateStore(State(foo = 0), scope)

        async {
            repeat(n) {
                store.set { copy(foo = foo + 1) }
            }
        }

        // One more scope for subscribers, to ensure subscribers are finished before cancelling store scope
        coroutineScope {
            repeat(subscribers) {
                async {
                    // Since only increase by 1 reducers are applied
                    // it's expected to see monotonously increasing sequence with no missing values
                    store.flow.takeWhile { it.foo < n }.toList().zipWithNext { a, b ->
                        Assert.assertEquals(a.foo + 1, b.foo)
                    }
                }
            }
        }
        scope.cancel()
    }

    /**
     * Tests that cancellation during first emit in CoroutinesStateStore.flow doesn't block other collectors forever
     * Will fail if stateChannel subscription will be collected without finally block in CoroutinesStateStore.flow builder
     */
    @Test(timeout = 10_000)
    @Suppress("DeferredResultUnused", "LocalVariableName")
    fun testProperCancellation() = runBlocking {
        val scope = CoroutineScope(Dispatchers.Default + Job())
        val store = CoroutinesStateStore(State(foo = 0), scope)

        val collectJob = async(start = CoroutineStart.UNDISPATCHED) {
            store.flow.collect {
                delay(Long.MAX_VALUE)
            }
        }
        collectJob.cancel()

        val n = 200
        coroutineScope {
            async(start = CoroutineStart.UNDISPATCHED) {
                store.flow.takeWhile { it.foo < n }.collect {
                    // no-op
                }
            }
            async {
                repeat(n) {
                    store.set { copy(foo = foo + 1) }
                }
            }
        }
        scope.cancel()
    }
}
