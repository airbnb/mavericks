package com.airbnb.mvrx

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.os.Handler
import android.os.Looper
import android.os.Message
import kotlin.reflect.KProperty1

// Set of MvRxView identity hash codes that have a pending invalidate.
private val PENDING_INVALIDATES = HashSet<Int>()
private val HANDLER = Handler(Looper.getMainLooper(), Handler.Callback { message ->
    val view = message.obj as MvRxView
    PENDING_INVALIDATES.remove(System.identityHashCode(view))
    if (view.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) view.invalidate()
    true
})

/**
 * Implement this in your MvRx capable Fragment.
 *
 * When you get a ViewModel with fragmentViewModel, activityViewModel, or existingViewModel, it
 * will automatically subscribe to all state changes in the ViewModel and call [invalidate].
 */
interface MvRxView : MvRxViewModelStoreOwner, LifecycleOwner {
    /**
     * Override this to handle any state changes from MvRxViewModels created through MvRx Fragment delegates.
     */
    fun invalidate()

    fun postInvalidate() {
        if (PENDING_INVALIDATES.add(System.identityHashCode(this@MvRxView))) {
            HANDLER.sendMessage(Message.obtain(HANDLER, System.identityHashCode(this@MvRxView), this@MvRxView))
        }
    }

    /**
     * Subscribes to all state updates for the given viewModel.
     *
     * @param uniqueOnly If true, when this MvRxView goes from a stopped to start lifecycle a state value
     * will only be emitted if the state changed. This is useful for transient views that should only
     * be shown once (toasts, poptarts), or logging. Most other views should use false, as when a view is destroyed
     * and recreated the previous state is necessary to recreate the view.
     *
     * Default: false.
     */
    fun <S : MvRxState> BaseMvRxViewModel<S>.subscribe(uniqueOnly: Boolean = false, subscriber: (S) -> Unit) = subscribe(this@MvRxView, uniqueOnly, subscriber)

    /**
     * Subscribes to state changes for only a specific property and calls the subscribe with
     * only that single property.
     *
     * @param uniqueOnly If true, when this MvRxView goes from a stopped to start lifecycle a state value
     * will only be emitted if the state changed. This is useful for transient views that should only
     * be shown once (toasts, poptarts), or logging. Most other views should use false, as when a view is destroyed
     * and recreated the previous state is necessary to recreate the view.
     *
     * Default: false.
     */
    fun <S : MvRxState, A> BaseMvRxViewModel<S>.selectSubscribe(
        prop1: KProperty1<S, A>,
        uniqueOnly: Boolean = false,
        subscriber: (A) -> Unit
    ) = selectSubscribe(this@MvRxView, prop1, uniqueOnly, subscriber)

    /**
     * Subscribe to changes in an async property. There are optional parameters for onSuccess
     * and onFail which automatically unwrap the value or error.
     *
     * @param uniqueOnly If true, when this MvRxView goes from a stopped to start lifecycle a state value
     * will only be emitted if the state changed. This is useful for transient views that should only
     * be shown once (toasts, poptarts), or logging. Most other views should use false, as when a view is destroyed
     * and recreated the previous state is necessary to recreate the view.
     *
     * Default: false.
     */
    fun <S : MvRxState, T> BaseMvRxViewModel<S>.asyncSubscribe(
        asyncProp: KProperty1<S, Async<T>>,
        uniqueOnly: Boolean = false,
        onFail: ((Throwable) -> Unit)? = null,
        onSuccess: ((T) -> Unit)? = null
    ) = asyncSubscribe(this@MvRxView, asyncProp, uniqueOnly, onFail, onSuccess)

    /**
     * Subscribes to state changes for two properties.
     *
     * @param uniqueOnly If true, when this MvRxView goes from a stopped to start lifecycle a state value
     * will only be emitted if the state changed. This is useful for transient views that should only
     * be shown once (toasts, poptarts), or logging. Most other views should use false, as when a view is destroyed
     * and recreated the previous state is necessary to recreate the view.
     *
     * Default: false.
     */
    fun <S : MvRxState, A, B> BaseMvRxViewModel<S>.selectSubscribe(
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        uniqueOnly: Boolean = false,
        subscriber: (A, B) -> Unit
    ) = selectSubscribe(this@MvRxView, prop1, prop2, uniqueOnly, subscriber)

    /**
     * Subscribes to state changes for three properties.
     *
     * @param uniqueOnly If true, when this MvRxView goes from a stopped to start lifecycle a state value
     * will only be emitted if the state changed. This is useful for transient views that should only
     * be shown once (toasts, poptarts), or logging. Most other views should use false, as when a view is destroyed
     * and recreated the previous state is necessary to recreate the view.
     *
     * Default: false.
     */
    fun <S : MvRxState, A, B, C> BaseMvRxViewModel<S>.selectSubscribe(
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        uniqueOnly: Boolean = false,
        subscriber: (A, B, C) -> Unit
    ) = selectSubscribe(this@MvRxView, prop1, prop2, prop3, uniqueOnly, subscriber)

    /**
     * Subscribes to state changes for four properties.
     *
     * @param uniqueOnly If true, when this MvRxView goes from a stopped to start lifecycle a state value
     * will only be emitted if the state changed. This is useful for transient views that should only
     * be shown once (toasts, poptarts), or logging. Most other views should use false, as when a view is destroyed
     * and recreated the previous state is necessary to recreate the view.
     *
     * Default: false.
     */
    fun <S : MvRxState, A, B, C, D> BaseMvRxViewModel<S>.selectSubscribe(
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        prop4: KProperty1<S, D>,
        uniqueOnly: Boolean = false,
        subscriber: (A, B, C, D) -> Unit
    ) = selectSubscribe(this@MvRxView, prop1, prop2, prop3, prop4, uniqueOnly, subscriber)
}