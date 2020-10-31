package com.airbnb.mvrx

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class CoroutinesStateStore<S : MavericksState>(
    initialState: S,
    private val scope: CoroutineScope,
    private val contextOverride: CoroutineContext = EmptyCoroutineContext
) : MavericksStateStore<S> {

    private val setStateChannel = Channel<S.() -> S>(capacity = Channel.UNLIMITED)
    private val withStateChannel = Channel<(S) -> Unit>(capacity = Channel.UNLIMITED)

    private val stateSharedFlow = MutableSharedFlow<S>(
        replay = 1,
        extraBufferCapacity = SubscriberBufferSize,
        onBufferOverflow = BufferOverflow.SUSPEND,
    ).apply { tryEmit(initialState) }

    @Volatile
    override var state = initialState

    /**
     * Returns a [Flow] for this store's state. It will begin by immediately emitting
     * the latest set value and then continue with all subsequent updates.
     *
     * This doesn't need distinctUntilChanged() because the de-dupinng is done once
     * for all subscriptions in [flushQueuesOnce].
     *
     * This flow never completes
     */
    override val flow: Flow<S> = stateSharedFlow.asSharedFlow()

    init {
        setupTriggerFlushQueues(scope)
    }

    /**
     * Poll [withStateChannel] and [setStateChannel] to respond to set/get state requests.
     */
    private fun setupTriggerFlushQueues(scope: CoroutineScope) {
        if (MavericksTestOverrides.FORCE_SYNCHRONOUS_STATE_STORES) return

        scope.launch(flushDispatcher + contextOverride) {
            while (isActive) {
                flushQueuesOnce()
            }
        }
    }

    /**
     * Flush the setState and withState queues.
     * All pending setState reducers will be run prior to every single withState lambda.
     * This ensures that situations such as the following will work correctly:
     *
     * Situation 1
     *
     * setState { ... }
     * withState { ... }
     *
     * Situation 2
     *
     * withState {
     *     setState { ... }
     *     withState { ... }
     * }
     */
    private suspend fun flushQueuesOnce() {
        select<Unit> {
            setStateChannel.onReceive { reducer ->
                val newState = state.reducer()
                if (newState != state) {
                    state = newState
                    stateSharedFlow.emit(newState)
                }
            }
            withStateChannel.onReceive { block ->
                block(state)
            }
        }
    }

    private fun flushQueuesOnceBlocking() {
        if (scope.isActive) {
            runBlocking { flushQueuesOnce() }
        }
    }

    override fun get(block: (S) -> Unit) {
        withStateChannel.offer(block)
        if (MavericksTestOverrides.FORCE_SYNCHRONOUS_STATE_STORES) {
            flushQueuesOnceBlocking()
        }
    }

    override fun set(stateReducer: S.() -> S) {
        setStateChannel.offer(stateReducer)
        if (MavericksTestOverrides.FORCE_SYNCHRONOUS_STATE_STORES) {
            flushQueuesOnceBlocking()
        }
    }

    companion object {
        private val flushDispatcher = Executors.newCachedThreadPool().asCoroutineDispatcher()

        /**
         * The buffer size that will be allocated by [MutableSharedFlow].
         * If it falls behind by more than 64 state updates, it will start suspending.
         * Slow consumers should consider using `stateFlow.buffer(onBufferOverflow = BufferOverflow.DROP_OLDEST)`.
         *
         * The internally allocated buffer is replay + extraBufferCapacity but always allocates 2^n space.
         * We use replay=1 so buffer = 64-1.
         */
        internal const val SubscriberBufferSize = 63
    }
}
