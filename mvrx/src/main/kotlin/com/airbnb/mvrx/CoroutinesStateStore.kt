package com.airbnb.mvrx

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.yield
import java.util.concurrent.Executors

@Suppress("EXPERIMENTAL_API_USAGE")
class CoroutinesStateStore<S : MvRxState>(
        initialState: S,
        scope: CoroutineScope
) : MvRxStateStore<S> {

    private val setStateChannel = Channel<S.() -> S>(capacity = Channel.UNLIMITED)
    private val withStateChannel = Channel<(S) -> Unit>(capacity = Channel.UNLIMITED)

    /**
     * This combination of BroadcastChannel, mutable state property and channelFlow was arrived
     * at after multiple attempts. This code needs to:
     * 1) Multicast
     * 2) Never fail to deliver an intermediate state to subscribers even if updates are fast or
     *    some subscribers are slow.
     * 3) Be able to provide an initial value.
     *
     * StateFlow can't be used because it conflates new sets. If you set its value in a tight
     * loop and listen to changes on a flow, you will miss values. ConflatedBroadcastChannel
     * alone has the same issue.
     *
     * A normal Channel can't be used because it isn't multicast.
     */
    private val stateChannel = BroadcastChannel<S>(capacity = Channel.BUFFERED)
    override var state = initialState
    override val flow: Flow<S> get() = channelFlow {
        send(state)
        stateChannel.consumeEach { send(it) }
    }.distinctUntilChanged()

    init {
        setupTriggerFlushQueues(scope)
        scope.coroutineContext[Job]?.invokeOnCompletion {
            setStateChannel.close()
            withStateChannel.close()
        }
    }

    /**
     * Observe [flushQueuesChannel] and flush queues whenever there is a new item.
     * This no-ops if [MvRxTestOverrides.FORCE_SYNCHRONOUS_STATE_STORES] is set.
     */
    private fun setupTriggerFlushQueues(scope: CoroutineScope) {
        if (MvRxTestOverrides.FORCE_SYNCHRONOUS_STATE_STORES) {
            scope.launch(Dispatchers.Unconfined) {
                flushQueues()
            }
            return
        }

        val executor = Executors.newSingleThreadExecutor()
        scope.coroutineContext[Job]!!.invokeOnCompletion {
            executor.shutdownNow()
        }

        scope.launch(executor.asCoroutineDispatcher()) {
            flushQueues()
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
    private suspend fun flushQueues() {
        while (true) {
            yield()
            select<Unit> {
                setStateChannel.onReceive { reducer ->
                    val newState = state.reducer()
                    stateChannel.offer(newState)
                    state = newState
                }
                withStateChannel.onReceive { block ->
                    block(state)
                }
            }
        }
    }

    private fun flushQueuesBlocking() = runBlocking { flushQueues() }

    override fun get(block: (S) -> Unit) {
        withStateChannel.offer(block)
    }

    override fun set(stateReducer: S.() -> S) {
        setStateChannel.offer(stateReducer)
    }
}