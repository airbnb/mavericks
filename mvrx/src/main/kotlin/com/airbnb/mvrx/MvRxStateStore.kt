package com.airbnb.mvrx

import android.arch.lifecycle.LifecycleOwner
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import java.util.LinkedList

/**
 * This is a container class around the actual state itself. It has a few optimizations to ensure
 * safe usage of state.
 *
 * All state reducers are run in a single background thread to ensure that they don't have race
 * conditions with each other.
 *
 * When subscribers get notified of state changes, the store will stop processing state updates
 * until the subscriber callback completes. This will prevent the state from updating in the middle
 * of the subscription callback.
 */
open class MvRxStateStore<S : Any>(initialState: S) : Disposable {
    /**
     * The subject is where state changes should be pushed to.
     */
    private val subject: Subject<S> = BehaviorSubject.create<S>().toSerialized()
    /**
     * The observable observes the subject but only emits events when the state actually changed.
     */
    private val observable: Observable<S> = subject.distinctUntilChanged()
    private val defaultObserveOnScheduler = AndroidSchedulers.mainThread()
    private val disposables = CompositeDisposable()

    /**
     * A subject that is used to flush the setState and getState queue. The value emitted on the subject is
     * not used. It is only used as a signal to flush the queues.
     */
    private val flushQueueSubject = BehaviorSubject.create<Unit>()

    private var setStateQueue = LinkedList<S.() -> S>()
    private val getStateQueue = LinkedList<(state: S) -> Unit>()

    /**
     * This is automatically updated from a subscription on the subject for easy access to the
     * current state.
     */
    @Volatile var state = initialState
        private set

    init {
        subject.onNext(initialState)

        flushQueueSubject.observeOn(Schedulers.newThread())
            // We don't want race conditions with setting the state on multiple background threads
            // simultaneously in which two state reducers get the same initial state to reduce.
            .subscribe({ _ -> flushQueues() }, ::handleError)
            // Ensure that state updates don't get processes after dispose.
            .registerDisposable()
    }

    /**
     * Get the current state. The block of code is posted to a queue and all pending setState blocks
     * are guaranteed to run before the get block is run.
     */
    fun get(block: (S) -> Unit) {
        synchronized(this) {
            getStateQueue.push(block)
        }
        flushQueueSubject.onNext(Unit)
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
    fun set(stateReducer: S.() -> S) {
        synchronized(this) {
            setStateQueue.push(stateReducer)
        }
        flushQueueSubject.onNext(Unit)
    }

    /**
     * @param lifecycleOwner The [LifecycleOwner] of the class which will be subscribing to this [MvRxStateStore].
     * Leave it out or  set it to null if you aren't subscribing from the context of a [LifecycleOwner].
     * @param shouldUpdate An optional callback to receive the previous state and new one to determine whether to notify the subscriber.
     * @param subscriber A callback that will be triggered every time the [MvRxStateStore] receives an update.
     * @return A [Disposable] to terminate the subscription early.
     */
    fun subscribe(
        lifecycleOwner: LifecycleOwner? = null,
        observerScheduler: Scheduler = defaultObserveOnScheduler,
        shouldUpdate: ((S, S) -> Boolean)? = null,
        subscriber: (S) -> Unit
    ): Disposable {
        val observable = observableFor(observerScheduler, shouldUpdate).map { it.second }

        if (lifecycleOwner == null) return observable.subscribe(subscriber)

        val lifecycleAwareObserver = MvRxLifecycleAwareObserver.Builder<S>(lifecycleOwner)
            .onNext(subscriber)
            .build()
        return observable.subscribeWith(lifecycleAwareObserver)
    }

    /**
     * Just like subscribe except your subscriber will receive two parameters, the previous
     * state and the new state.
     *
     * @see subscribe
     */
    fun subscribeWithHistory(
        lifecycleOwner: LifecycleOwner? = null,
        observerScheduler: Scheduler = defaultObserveOnScheduler,
        shouldUpdate: ((S, S) -> Boolean)? = null,
        subscriber: (S, S) -> Unit
    ): Disposable {
        val observable = observableFor(observerScheduler, shouldUpdate)

        if (lifecycleOwner == null) return observable.subscribe { pair -> subscriber(pair.first, pair.second) }

        val lifecycleAwareObserver = MvRxLifecycleAwareObserver.Builder<Pair<S, S>>(lifecycleOwner)
            .onNext { pair -> subscriber(pair.first, pair.second) }
            .build()
        return observable.subscribeWith(lifecycleAwareObserver)
    }

    /**
     * Flushes the setState and getState queues.
     *
     * This will flush he setState queue then call the first element on the getState queue.
     *
     * In case the setState queue calls setState, we call flushQueues recursively to flush the setState queue
     * in between every getState block gets processed.
     */
    private fun flushQueues() {
        flushSetStateQueue()
        val block = synchronized(this) {
            if (getStateQueue.isEmpty()) return
            getStateQueue.removeFirst()
        }
        block(state)
        flushQueues()
    }

    /**
     * Coalesce all updates on the setState queue and clear the queue.
     */
    private fun flushSetStateQueue() {
        synchronized(this) {
            if (setStateQueue.isEmpty()) return
            val queue = setStateQueue
            setStateQueue = LinkedList()
            queue
        }
            .fold(state) { state, reducer -> state.reducer() }
            .run {
                state = this
                subject.onNext(this)
            }
    }

    private fun handleError(throwable: Throwable) {
        // Throw the root cause to remove all of the rx stacks.
        // TODO: better error handling
        var e: Throwable? = throwable
        while (e?.cause != null) e = e.cause
        e?.let { throw it }
    }

    private fun observableFor(observerScheduler: Scheduler, shouldUpdate: ((S, S) -> Boolean)? = null): Observable<Pair<S, S>> {
        val shouldUpdateWithDefault = shouldUpdate ?: { _, _ -> true }
        return this.observable
            // Map the current state to a pair so that it has the same type as the scan accumulator which is a pair of (old state, new state)
            .map { it to it }
            // Accumulator is a pair of (old state, new state)
            .scan { accumulator, currentState -> accumulator.second to currentState.first }
            .filter { shouldUpdateWithDefault(it.first, it.second) }
            .observeOn(observerScheduler)
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
