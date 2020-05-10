package com.airbnb.mvrx

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@Suppress("EXPERIMENTAL_API_USAGE")
class CoroutinesStateStore<S : MvRxState>(
    initialState: S,
    private val scope: CoroutineScope = CoroutineScope(Job())
) : MvRxStateStore<S> {

    /** Channel that serves as a trigger to flush the setState and withState queues. */
    private val flushQueuesChannel = Channel<Unit>(capacity = Channel.CONFLATED)
    private val setStateChannel = Channel<S.() -> S>(capacity = Channel.UNLIMITED)
    private val withStateChannel = Channel<(S) -> Unit>(capacity = Channel.UNLIMITED)

    private val stateFlow = MutableStateFlow(initialState)
    override val state: S get() = stateFlow.value
    // Buffer will ensure that subscribers gets all intermediate states even if they are slower
    // then new states are published. 50 is an arbitrary number.
    override val flow: StateFlow<S> get() = stateFlow

    init {
        setupTriggerFlushQueues()
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
    private fun setupTriggerFlushQueues() {
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
    private fun flushQueues() {
        while (!setStateChannel.isEmpty || !withStateChannel.isEmpty) {
            var reducer = setStateChannel.poll()
            while (reducer != null) {
                stateFlow.value = state.reducer()
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
            flushQueues()
        } else {
            flushQueuesChannel.offer(Unit)
        }
    }

    override fun set(stateReducer: S.() -> S) {
        setStateChannel.offer(stateReducer)
        if (MvRxTestOverrides.FORCE_SYNCHRONOUS_STATE_STORES) {
            flushQueues()
        } else {
            flushQueuesChannel.offer(Unit)
        }
    }
}