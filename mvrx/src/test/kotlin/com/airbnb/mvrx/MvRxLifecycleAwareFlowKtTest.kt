package com.airbnb.mvrx

import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Test

@ExperimentalCoroutinesApi
class MvRxLifecycleAwareFlowKtTest {
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
}