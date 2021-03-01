package com.airbnb.mvrx

import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Test

@ExperimentalCoroutinesApi
class MavericksLifecycleAwareFlowKtTest : BaseTest() {
    @Test
    fun testDoesntFlowFromCreate() = runBlockingTest {
        val flow = flowOf(1)
        val owner = TestLifecycleOwner()
        val values = mutableListOf<Int>()
        val job = flow.flowWhenStarted(owner).onEach {
            values += it
        }.launchIn(this)
        assertEquals(emptyList<Int>(), values)
        job.cancel()
    }

    @Test
    fun testFlowsFromStart() = runBlockingTest {
        val flow = flowOf(1)
        val owner = TestLifecycleOwner()
        val values = mutableListOf<Int>()
        owner.lifecycle.currentState = Lifecycle.State.STARTED
        val job = flow.flowWhenStarted(owner).onEach {
            values += it
        }.launchIn(this)
        assertEquals(listOf(1), values)
        job.cancel()
    }

    @Test
    fun testFlowsWhenStarted() = runBlockingTest {
        val channel = Channel<Int>(Channel.UNLIMITED)
        channel.send(1)
        val owner = TestLifecycleOwner()
        val values = mutableListOf<Int>()
        val job = channel.consumeAsFlow().flowWhenStarted(owner).onEach {
            values += it
        }.launchIn(this)
        owner.lifecycle.currentState = Lifecycle.State.STARTED
        assertEquals(listOf(1), values)
        job.cancel()
        channel.close()
    }

    @Test
    fun testEmitsWhenRestarted() = runBlockingTest {
        val channel = Channel<Int>(Channel.UNLIMITED)
        channel.send(1)
        val owner = TestLifecycleOwner()
        val values = mutableListOf<Int>()
        val job = channel.consumeAsFlow().flowWhenStarted(owner).onEach {
            values += it
        }.launchIn(this)
        owner.lifecycle.currentState = Lifecycle.State.STARTED
        // Add a small delay. flowWhenStarted() has the edge case where lifecycles get conflated
        // by its internal StateFlow so if the state changes from stopped to started too quickly,
        // it may not re-emit. However, this shouldn't be a problem in practice.
        delay(1)
        owner.lifecycle.currentState = Lifecycle.State.CREATED
        delay(1)
        owner.lifecycle.currentState = Lifecycle.State.STARTED
        assertEquals(listOf(1, 1), values)
        job.cancel()
        channel.close()
    }

    @Test
    fun testStateUpdateHasHigherPriority() = runBlockingTest {
        val owner = TestLifecycleOwner()
        owner.lifecycle.currentState = Lifecycle.State.STARTED
        val values = mutableListOf<Int>()
        val channel = Channel<Int>(Channel.UNLIMITED)
        channel.send(0)
        val job = channel.consumeAsFlow().flowWhenStarted(owner).onEach { value ->
            values += value
            if (value == 0) {
                // add 1,2,3,4,5 to channel and stop lifecycle, so none of these values should be collected
                repeat(5) { channel.send(it + 1) }
                owner.lifecycle.currentState = Lifecycle.State.CREATED
            }
        }.launchIn(this)
        delay(1)

        assertEquals(listOf(0), values)
        job.cancel()
    }

    @Test
    fun testAllValuesCollectedIfLifecycleWasStarted() = runBlockingTest {
        val owner = TestLifecycleOwner()
        owner.lifecycle.currentState = Lifecycle.State.STARTED
        val values = mutableListOf<Int>()
        // Since lifecycle in started, we should collect all three values, not only last
        val job = flowOf(1, 2, 3).flowWhenStarted(owner).onEach { value ->
            values += value
        }.launchIn(this)
        delay(1)

        assertEquals(listOf(1, 2, 3), values)
        job.cancel()
    }

    @Test
    fun testFlowCompletedIfLifecycleDestroyed() = runBlocking {
        val owner = TestLifecycleOwner()
        owner.lifecycle.currentState = Lifecycle.State.STARTED

        val endlessFlow = flow {
            emit(0)
            delay(Long.MAX_VALUE)
        }

        val values = mutableListOf<Int>()
        val job = launch {
            endlessFlow.flowWhenStarted(owner)
                .collect {
                    println("$it")
                    values += it
                }
        }
        delay(1)
        owner.lifecycle.currentState = Lifecycle.State.DESTROYED

        job.join()
    }

    @Test
    fun testFlowCompleteIfSourceFlowCompleted() = runBlocking {
        val owner = TestLifecycleOwner()
        owner.lifecycle.currentState = Lifecycle.State.STARTED

        flowOf(1).flowWhenStarted(owner).collect()
    }
}