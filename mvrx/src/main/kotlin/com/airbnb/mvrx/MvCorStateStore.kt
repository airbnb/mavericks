package com.airbnb.mvrx

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.cancel
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.isActive
import kotlinx.coroutines.experimental.rx2.asObservable
import java.util.*
import kotlin.coroutines.experimental.CoroutineContext


/**
 * This is a container class around the actual state itself. It has a few optimizations to ensure
 * safe usage of state.
 *
 * All state reducers are run in an actor to ensure that they don't have race
 * conditions with each other.
 *
 */

internal open class MvCorStateStore<S : Any>(initialState: S, final override val coroutineContext: CoroutineContext = Dispatchers.Default) : Disposable, CoroutineScope, StateStore<S> {

    /**
     * The stateChannel is where state changes should be pushed to.
     */
    private val stateChannel = ConflatedBroadcastChannel(initialState)
    /**
     * The observable observes the stateChannel but only emits events when the state actually changed.
     */

    override val observable: Observable<S> = stateChannel.openSubscription().asObservable(coroutineContext).distinctUntilChanged()
    /**
     * This is automatically updated from a subscription on the stateChannel for easy access to the
     * current state.
     */
    override val state: S
        get() = stateChannel.value

    sealed class Job<S> {
        class SetQueueElement<S>(val reducer: S.() -> S) : Job<S>()
        class GetQueueElement<S>(val block: (S) -> Unit) : Job<S>()
    }

    /**
     * Actor responsible for processing get and set blocks. Sequentially processes every block, SetQueueElement in higher priority.
     * Once every SetQueueElement is processed actor iterates over GetQueueElements until no elements are left, or a new message
     * is sent to an actor
     */
    private val actor = actor<Job<S>>(coroutineContext, capacity = Channel.UNLIMITED) {


        val getQueue = ArrayDeque<(S) -> Unit>()
        consumeEach { msg ->
            try {
                when (msg) {
                    is Job.GetQueueElement<S> -> getQueue.offer(msg.block)

                    is Job.SetQueueElement<S> -> stateChannel.value
                            .let { msg.reducer(it) }
                            .let(stateChannel::offer)
                }

                while (channel.isEmpty) {
                    stateChannel.value.let(getQueue.poll() ?: break)
                }

            } catch (t: Throwable) {
                handleError(t)
            }
        }
    }


    /**
     * Get the current state. The block of code is posted to a queue and all pending setState blocks
     * are guaranteed to run before the get block is run.
     */
    override fun get(block: (S) -> Unit) {
        actor.offer(Job.GetQueueElement(block))
    }

    /**
     * Call this to update the state. The state reducer will get added to a queue that is processed
     * on a specified dispatcher. The state reducer's receiver type is the current state when the
     * reducer is called.
     *
     * An example of a reducer would be `{ copy(myProperty = 5) }`. The copy comes from the copy
     * function on a Kotlin data class and can be called directly because state is the receiver type
     * of the reducer. In this case, it will also implicitly return the only expression so that is
     * all of the code required.
     */
    override fun set(stateReducer: S.() -> S) {
        actor.offer(Job.SetQueueElement(stateReducer))
    }


    private fun handleError(throwable: Throwable) {
        // Throw the root cause to remove all of the rx stacks.
        // TODO: better error handling
        var e: Throwable? = throwable
        while (e?.cause != null) e = e.cause
        e?.let { throw it }
    }

    override fun isDisposed() = coroutineContext.isActive

    override fun dispose() {
        coroutineContext.cancel()
    }

}
