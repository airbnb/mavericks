package com.airbnb.mvrx

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlinx.coroutines.experimental.channels.*
import kotlinx.coroutines.experimental.rx2.asObservable
import java.util.LinkedList

/**
 * This is a container class around the actual state itself. It has a few optimizations to ensure
 * safe usage of state.
 *
 * All state reducers are run in a single background thread to ensure that they don't have race
 * conditions with each other.
 *
 */

internal open class MvCorStateStore<S : Any>(initialState: S, coroutineDispatcher: CoroutineDispatcher = DefaultDispatcher) : Disposable, IMvRxStateStore<S> {
    /**
     * The channel is where state changes should be pushed to.
     */
    private val channel = ConflatedBroadcastChannel(initialState)
    /**
     * The observable observes the channel but only emits events when the state actually changed.
     */
    private val disposables = CompositeDisposable()


    override val observable: Observable<S> = channel.openSubscription().asObservable(coroutineDispatcher).distinctUntilChanged()
    /**
     * This is automatically updated from a subscription on the channel for easy access to the
     * current state.
     */
    override val state: S
        get() = channel.value

    sealed class Job<S> {
        class SetQueueElement<S>(val block: S.() -> S) : Job<S>()
        class GetQueueElement<S>(val block: (S) -> Unit) : Job<S>()
        class GetSyncValue<S>(val completable: CompletableDeferred<S>) : Job<S>()
    }

    val actor = actor<Job<S>>(context = coroutineDispatcher, capacity = Channel.UNLIMITED){


        val getStateQueue = LinkedList<(state: S) -> Unit>()
        var setStateQueue = LinkedList<S.() -> S>()

        fun dequeueGetStateBlock(): ((state: S) -> Unit)? {
            if (getStateQueue.isEmpty()) return null

            return getStateQueue.removeFirst()
        }

        fun dequeueAllSetStateBlocks(): List<(S.() -> S)>? {
            // do not allocate empty queue for no-op flushes
            if (setStateQueue.isEmpty()) return null

            val queue = setStateQueue
            setStateQueue = LinkedList()
            return queue
        }

        /**
         * Coalesce all updates on the setState queue and clear the queue.
         */
        fun flushSetStateQueue() {
            val blocks = dequeueAllSetStateBlocks() ?: return

            blocks
                    .fold(this@MvCorStateStore.channel.value) { state, reducer -> state.reducer() }
                    .run { this@MvCorStateStore.channel.offer(this) }
        }

        /**
         * Flushes the setState and getState queues.
         *
         * This will flush he setState queue then call the first element on the getState queue.
         *
         * In case the setState queue calls setState, we call flushQueues recursively to flush the setState queue
         * in between every getState block gets processed.
         */
        tailrec fun flushQueues() {
            flushSetStateQueue()
            val block = dequeueGetStateBlock() ?: return
            block(this@MvCorStateStore.channel.value)
            flushQueues()
        }


        loop@ for (msg in channel) { // iterate over incoming messages

            when (msg) {
                is Job.GetQueueElement<S> -> {
                    getStateQueue.push(msg.block)
                }
                is Job.SetQueueElement<S> -> {
                    setStateQueue.push(msg.block)
                }
                is Job.GetSyncValue<S> -> {
                    msg.completable.complete(this@MvCorStateStore.channel.value)
                    continue@loop
                }
            }

            try {
                flushQueues()
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
     * Call this to update the state. The state reducer will get added to a queue that is processes
     * on a background thread. The state reducer's receiver type is the current state when the
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
