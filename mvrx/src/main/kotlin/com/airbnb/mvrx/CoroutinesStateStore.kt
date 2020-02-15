package com.airbnb.mvrx

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

@Suppress("EXPERIMENTAL_API_USAGE")
class CoroutinesStateStore<S : MvRxState>(
    initialState: S,
    private val scope: CoroutineScope = createCoroutineScope()
) : MvRxStateStore<S> {

    private val setStateChannel = Channel<S.() -> S>(capacity = Channel.UNLIMITED)
    private val withStateChannel = Channel<(S) -> Unit>(capacity = Channel.UNLIMITED)
    private val flushQueuesChannel = Channel<Unit>(capacity = Channel.CONFLATED)

    private val stateChannel = ConflatedBroadcastChannel<S>(initialState)
    override val state: S get() = stateChannel.valueOrNull ?: error("There is no state yet! This should never happen.")

    init {
        scope.launch {
            flushQueuesChannel.consumeEach {
                while (!setStateChannel.isEmpty || !withStateChannel.isEmpty) {
                    var reducer = setStateChannel.poll()
                    while (reducer != null) {
                        stateChannel.offer(state.reducer())
                        reducer = setStateChannel.poll()
                    }

                    withStateChannel.poll()?.let { withStateBlock ->
                        withStateBlock(state)
                    }
                }
            }
        }
    }

    override fun get(block: (S) -> Unit) {
        if (!scope.isActive) return
        withStateChannel.offer(block)
        flushQueuesChannel.offer(Unit)
    }

    override fun set(stateReducer: S.() -> S) {
        if (!scope.isActive) return
        setStateChannel.offer(stateReducer)
        flushQueuesChannel.offer(Unit)
    }

    override val flow: Flow<S> get() = stateChannel.asFlow().distinctUntilChanged()

    override fun cancel() {
        scope.cancel()
        withStateChannel.cancel()
        flushQueuesChannel.cancel()
        stateChannel.cancel()
    }

    companion object {
        private val threadCount = AtomicInteger(1)
        private val defaultScopeFactory = {
            val executor = Executors.newSingleThreadExecutor()
            val job = Job()
            job.invokeOnCompletion {
                executor.shutdown()
            }
            CoroutineScope(executor.asCoroutineDispatcher() + job)
        }

        var scopeFactory: (() -> CoroutineScope)? = null

        fun createCoroutineScope() = (scopeFactory ?: defaultScopeFactory).invoke()
    }
}