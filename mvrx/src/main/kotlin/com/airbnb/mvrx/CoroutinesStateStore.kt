package com.airbnb.mvrx

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext

@Suppress("EXPERIMENTAL_API_USAGE")
class CoroutinesStateStore<S : MvRxState>(
    initialState: S,
    private val scope: CoroutineScope = coroutineScope()
) : MvRxStateStore<S> {

    private val setStateChannel = Channel<S.() -> S>(capacity = Channel.UNLIMITED)
    private val withStateChannel = Channel<(S) -> Unit>(capacity = Channel.UNLIMITED)
    private val flushQueuesChannel = Channel<Unit>(capacity = Channel.CONFLATED)

    private val stateChannel = ConflatedBroadcastChannel<S>(initialState)

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

    override val state get() = stateChannel.valueOrNull ?: error("There is no state yet! This should never happen.")

    override fun get(block: (S) -> Unit) {
        withStateChannel.offer(block)
        flushQueuesChannel.offer(Unit)
    }

    override fun set(stateReducer: S.() -> S) {
        setStateChannel.offer(stateReducer)
        flushQueuesChannel.offer(Unit)
    }

    override val flow get() = stateChannel.asFlow().distinctUntilChanged()

    override fun cancel() {
        scope.coroutineContext.cancel()
        withStateChannel.cancel()
        flushQueuesChannel.cancel()
        stateChannel.cancel()
    }

    companion object {
        var scopeFactory: (() -> CoroutineScope)? = null

        fun coroutineScope() = scopeFactory?.invoke() ?: CoroutineScope(newSingleThreadContext("CoroutineStateStore") + SupervisorJob())
    }
}