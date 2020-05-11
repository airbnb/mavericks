package com.airbnb.mvrx

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors

@Suppress("EXPERIMENTAL_API_USAGE")
class CoroutinesStateStore<S : MvRxState>(
    initialState: S,
    scope: CoroutineScope
) : MvRxStateStore<S> {

    /** Channel that serves as a trigger to flush the setState and withState queues. */
    private val flushQueuesChannel = Channel<Unit>(capacity = Channel.CONFLATED)
    private val setStateChannel = Channel<S.() -> S>(capacity = Channel.UNLIMITED)
    private val withStateChannel = Channel<(S) -> Unit>(capacity = Channel.UNLIMITED)

    private val stateChannel = BroadcastChannel<S>(capacity = Channel.BUFFERED)
    override var state = initialState
    override val flow: Flow<S> get() = channelFlow {
        send(state)
        stateChannel.consumeEach { send(it) }
    }.distinctUntilChanged()

    init {
        setupTriggerFlushQueues(scope)
        scope.coroutineContext[Job]?.invokeOnCompletion {
            flushQueuesChannel.close()
            setStateChannel.close()
            withStateChannel.close()
        }
    }

    /**
     * Observe [flushQueuesChannel] and flush queues whenever there is a new item.
     * This no-ops if [MvRxTestOverrides.FORCE_SYNCHRONOUS_STATE_STORES] is set.
     */
    private fun setupTriggerFlushQueues(scope: CoroutineScope) {
        if (MvRxTestOverrides.FORCE_SYNCHRONOUS_STATE_STORES) return

        val executor = Executors.newSingleThreadExecutor()
        scope.coroutineContext[Job]!!.invokeOnCompletion {
            executor.shutdownNow()
        }

        scope.launch(executor.asCoroutineDispatcher()) {
            flushQueuesChannel.consumeEach {
                flushQueues()
            }
        }
    }

    /**
     * Flush the setState and withState queues.
     * All pending setState reducers will be run prior to every single withState lambda.
     * This ensures that situations such as the following will work correctly:
     *
     * Sitaution 1
     *
     * setState { ... }
     * withState { ... }
     *
     * Sitaution 2
     *
     * withState {
     *     setState { ... }
     *     withState { ... }
     * }
     */
    @Synchronized
    private suspend fun flushQueues() {
        while (!setStateChannel.isEmpty || !withStateChannel.isEmpty) {
            var reducer = setStateChannel.poll()
            while (reducer != null) {
                val newState = state.reducer()
                stateChannel.send(newState)
                state = newState
                reducer = setStateChannel.poll()
            }

            withStateChannel.poll()?.let { withStateBlock ->
                withStateBlock(state)
            }
        }
    }

    override fun get(block: (S) -> Unit) {
        withStateChannel.offer(block)
        if (MvRxTestOverrides.FORCE_SYNCHRONOUS_STATE_STORES) {
            runBlocking { flushQueues() }
        } else {
            flushQueuesChannel.offer(Unit)
        }
    }

    override fun set(stateReducer: S.() -> S) {
        setStateChannel.offer(stateReducer)
        if (MvRxTestOverrides.FORCE_SYNCHRONOUS_STATE_STORES) {
            runBlocking { flushQueues() }
        } else {
            flushQueuesChannel.offer(Unit)
        }
    }
}