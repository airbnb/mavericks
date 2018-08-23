package com.airbnb.mvrx

import android.arch.lifecycle.*
import android.arch.lifecycle.Lifecycle.Event
import android.arch.lifecycle.Lifecycle.State
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.functions.*
import io.reactivex.internal.disposables.DisposableHelper
import io.reactivex.internal.functions.Functions
import io.reactivex.internal.observers.LambdaObserver
import java.util.concurrent.atomic.*

/**
 * An wrapper around an [Observer] associated with a [LifecycleOwner]. It has an [activeState], and when in a lifecycle state greater
 * than the [activeState] (as defined by [Lifecycle.State.isAtLeast()]) it will deliver values to the [sourceObserver] or [onNext] lambda.
 * When in a lower lifecycle state, the most recent update will be saved, and delivered when active again.
 */
internal class MvRxLifecycleAwareObserver<T>(
        owner: LifecycleOwner,
        private val activeState: Lifecycle.State = DEFAULT_ACTIVE_STATE,
        private val alwaysDeliverLastValueWhenUnlocked: Boolean = false,
        private val sourceObserver: Observer<T>) : AtomicReference<Disposable>(), LifecycleObserver, Observer<T>, Disposable {

    constructor(
            owner: LifecycleOwner,
            activeState: Lifecycle.State = DEFAULT_ACTIVE_STATE,
            alwaysDeliverLastValueWhenUnlocked: Boolean = false,
            onComplete: Action = Functions.EMPTY_ACTION,
            onSubscribe: Consumer<in Disposable> = Functions.emptyConsumer(),
            onError: Consumer<in Throwable> = Functions.ON_ERROR_MISSING,
            onNext: Consumer<T> = Functions.emptyConsumer()
    ) : this(owner, activeState, alwaysDeliverLastValueWhenUnlocked, LambdaObserver<T>(onNext, onError, onComplete, onSubscribe))

    private var owner: LifecycleOwner? = owner
    private var lastUndeliveredValue: T? = null
    private var lastValue: T? = null
    private val locked = AtomicBoolean(true)

    override fun onSubscribe(d: Disposable) {
        if (DisposableHelper.setOnce(this, d)) {
            requireOwner().lifecycle.addObserver(this)
            sourceObserver.onSubscribe(this)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        requireOwner().lifecycle.removeObserver(this)
        owner = null
        if (!isDisposed) {
            dispose()
        }
    }

    @OnLifecycleEvent(Event.ON_ANY)
    fun onLifecycleEvent() {
        updateLock()
    }

    private fun updateLock() {
        if (owner?.lifecycle?.currentState?.isAtLeast(activeState) == true) {
            unlock()
        } else {
            lock()
        }
    }

    override fun onNext(t: T) {
        if (!locked.get()) {
            sourceObserver.onNext(t)
        } else {
            lastUndeliveredValue = t
        }
        lastValue = t
    }

    override fun onError(e: Throwable) {
        if (!isDisposed) {
            lazySet(DisposableHelper.DISPOSED)
            sourceObserver.onError(e)
        }
    }

    override fun onComplete() {
        sourceObserver.onComplete()
    }

    override fun dispose() {
        DisposableHelper.dispose(this)
    }

    override fun isDisposed(): Boolean {
        return get() === DisposableHelper.DISPOSED
    }

    private fun unlock() {
        if (!locked.getAndSet(false)) {
            return
        }
        if (!isDisposed) {
            val valueToDeliverOnUnlock = if (alwaysDeliverLastValueWhenUnlocked && lastValue != null) lastValue else lastUndeliveredValue
            lastUndeliveredValue = null
            if (valueToDeliverOnUnlock != null) {
                onNext(valueToDeliverOnUnlock)
            }
        }
    }

    private fun lock() {
        locked.set(true)
    }

    private fun requireOwner(): LifecycleOwner {
        return owner!!
    }

    companion object {

        private val DEFAULT_ACTIVE_STATE = State.STARTED

    }
}