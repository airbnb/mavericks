package com.airbnb.mvrx

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

class AsyncStateStoreTest {

    @ExperimentalTime
    @Test
    fun testFlowNotCompletedAfterScopeCancelled() = runBlocking {
        val scope = CoroutineScope(Job())
        val store = CoroutinesStateStore(MavericksStateStoreTestState(), scope)
        val collectJob = scope.launch(Job()) { store.flow.collect() }
        scope.cancel()
        assertNull(withTimeoutOrNull(1.seconds) {
            collectJob.join()
        })
    }
}
