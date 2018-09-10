package com.airbnb.mvrx

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.rx2.asObservable
import java.util.*

/**
 * This is a container class around the actual state itself. It has a few optimizations to ensure
 * safe usage of state.
 *
 * All state reducers are run in an actor to ensure that they don't have race
 * conditions with each other.
 *
 */

internal open class MvCorStateStore<S : Any>(initialState: S, coroutineDispatcher: CoroutineDispatcher = DefaultDispatcher) : Disposable, IMvRxStateStore<S> {
    /**
     * The stateChannel is where state changes should be pushed to.
     */
    private val stateChannel = ConflatedBroadcastChannel(initialState)
    /**
     * The observable observes the stateChannel but only emits events when the state actually changed.
     */

    private val disposables = CompositeDisposable()


    override val observable: Observable<S> = stateChannel.openSubscription().asObservable(coroutineDispatcher).distinctUntilChanged()
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

    private val actor = actor<Job<S>>(context = coroutineDispatcher, capacity = Channel.UNLIMITED) {

        val getQueue = LinkedList<(S)->Unit>()

        for (msg in channel) { // iterate over incoming messages
            try {
                when (msg) {
                    is Job.GetQueueElement<S> -> getQueue.push(msg.block)

                    is Job.SetQueueElement<S> -> stateChannel.value
                                .let { msg.reducer(it) }
                                .let(stateChannel::offer)
                }

                if(channel.isEmpty) {
                    getQueue.forEach { it(stateChannel.value) }
                    getQueue.clear()
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

    override fun isDisposed() = disposables.isDisposed

    override fun dispose() {
        disposables.dispose()
    }

    private fun Disposable.registerDisposable(): Disposable {
        disposables.add(this)
        return this
    }
}
