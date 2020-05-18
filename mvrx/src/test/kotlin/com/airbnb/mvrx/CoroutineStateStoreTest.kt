package com.airbnb.mvrx

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.util.concurrent.CountDownLatch

class AsyncStateStoreTest {

    @Test(timeout = 10_000)
    fun testFlowCompletedAfterScopeCancelled() = runBlocking {
        val scope = CoroutineScope(Job())
        val store = CoroutinesStateStore(MvRxStateStoreTestState(), scope)
        val collectJob = scope.launch(Job()) { store.flow.collect() }
        scope.cancel()
        collectJob.join()
    }

    @Test(timeout = 10_000)
    fun testFlowCompletedAfterScopeCancelledEvenWithSlowJob() = runBlocking {
        val scope = CoroutineScope(Job(coroutineContext[Job]))
        val store = CoroutinesStateStore(MvRxStateStoreTestState(), scope)
        val latch = CountDownLatch(1)
        scope.launch {
            latch.await()
        }
        val collectJob = scope.launch(Job()) {
            store.flow.collect()
        }
        scope.cancel()
        collectJob.join()
        latch.countDown()
    }

}
