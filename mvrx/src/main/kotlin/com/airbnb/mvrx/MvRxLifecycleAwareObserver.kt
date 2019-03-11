package com.airbnb.mvrx

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.Lifecycle.Event
import android.arch.lifecycle.Lifecycle.State
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.OnLifecycleEvent
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.internal.disposables.DisposableHelper
import io.reactivex.internal.functions.Functions
import io.reactivex.internal.observers.LambdaObserver
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * An wrapper around an [Observer] associated with a [LifecycleOwner]. It has an [activeState], and when in a lifecycle state greater
 * than the [activeState] (as defined by [Lifecycle.State.isAtLeast()]) it will deliver values to the [sourceObserver] or [onNext] lambda.
 * When in a lower lifecycle state, the most recent update will be saved, and delivered when active again.
 */
internal class MvRxLifecycleAwareObserver<T : Any>(
    private var owner: LifecycleOwner?,
    private val activeState: Lifecycle.State = DEFAULT_ACTIVE_STATE,
    private val deliveryMode: DeliveryMode = Standard,
    private var lastDeliveredValueFromPriorObserver: T?,
    private var sourceObserver: Observer<T>?
) : AtomicReference<Disposable>(), LifecycleObserver, Observer<T>, Disposable {

    constructor(
        owner: LifecycleOwner,
        activeState: Lifecycle.State = DEFAULT_ACTIVE_STATE,
        deliveryMode: DeliveryMode = Standard,
        lastDeliveredValue: T? = null,
        onComplete: Action = Functions.EMPTY_ACTION,
        onSubscribe: Consumer<in Disposable> = Functions.emptyConsumer(),
        onError: Consumer<in Throwable> = Functions.ON_ERROR_MISSING,
        onNext: Consumer<T> = Functions.emptyConsumer()
    ) : this(owner, activeState, deliveryMode, lastDeliveredValue, LambdaObserver<T>(onNext, onError, onComplete, onSubscribe))

    private var lastUndeliveredValue: T? = null
    private var lastValue: T? = null
    private val locked = AtomicBoolean(true)
    private val isUnlocked
        get() = !locked.get()
    private val deliveredFirstValue = AtomicBoolean(false)

    override fun onSubscribe(d: Disposable) {
        if (DisposableHelper.setOnce(this, d)) {
            requireOwner().lifecycle.addObserver(this)
            requireSourceObserver().onSubscribe(this)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        requireOwner().lifecycle.removeObserver(this)
        if (!isDisposed) {
            dispose()
        }
        owner = null
        sourceObserver = null
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

    override fun onNext(nextValue: T) {
        if (isUnlocked) {
            val suppressRepeatedFirstValue = !deliveredFirstValue.getAndSet(true)
                && deliveryMode is UniqueOnly
                && lastDeliveredValueFromPriorObserver == nextValue
            lastDeliveredValueFromPriorObserver = null
            if (!suppressRepeatedFirstValue) {
                requireSourceObserver().onNext(nextValue)
            }
        } else {
            lastUndeliveredValue = nextValue
        }
        lastValue = nextValue
    }

    override fun onError(e: Throwable) {
        if (!isDisposed) {
            lazySet(DisposableHelper.DISPOSED)
            requireSourceObserver().onError(e)
        }
    }

    override fun onComplete() {
        requireSourceObserver().onComplete()
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
            val valueToDeliverOnUnlock = when {
                deliveryMode is UniqueOnly -> lastUndeliveredValue
                deliveryMode is Standard && lastUndeliveredValue != null -> lastUndeliveredValue
                deliveryMode is Standard && lastUndeliveredValue == null -> lastValue
                else -> throw IllegalStateException("Value to deliver on unlock should be exhaustive.")
            }
            lastUndeliveredValue = null
            if (valueToDeliverOnUnlock != null) {
                onNext(valueToDeliverOnUnlock)
            }
        }
    }

    private fun lock() {
        locked.set(true)
    }

    private fun requireOwner(): LifecycleOwner = requireNotNull(owner) { "Cannot access lifecycleOwner after onDestroy." }

    private fun requireSourceObserver() = requireNotNull(sourceObserver) { "Cannot access observer after onDestroy." }

    companion object {
        private val DEFAULT_ACTIVE_STATE = State.STARTED
    }
}