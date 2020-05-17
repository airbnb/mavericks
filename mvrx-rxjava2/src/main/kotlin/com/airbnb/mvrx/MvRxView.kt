package com.airbnb.mvrx

import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import kotlin.reflect.KProperty1

/**
 * Implement this in your MvRx capable Fragment.
 *
 * When you get a ViewModel with fragmentViewModel, activityViewModel, or existingViewModel, it
 * will automatically subscribe to all state changes in the ViewModel and call [invalidate].
 */
interface MvRxView : MavericksView {

    /**
     * Subscribes to all state updates for the given viewModel.
     *
     * @param deliveryMode If [UniqueOnly] when this MvRxView goes from a stopped to started lifecycle a state value
     * will only be emitted if the state changed. This is useful for transient views that should only
     * be shown once (toasts, poptarts), or logging. Most other views should use false, as when a view is destroyed
     * and recreated the previous state is necessary to recreate the view.
     *
     * Use [uniqueOnly] to automatically create a [UniqueOnly] mode with a unique id for this view.
     *
     * Default: [RedeliverOnStart].
     */
    fun <S : MvRxState> BaseMvRxViewModel<S>.subscribe(deliveryMode: DeliveryMode = RedeliverOnStart, subscriber: (S) -> Unit) =
        subscribe(this@MvRxView.subscriptionLifecycleOwner, deliveryMode, subscriber)

    /**
     * Subscribes to state changes for only a specific property and calls the subscribe with
     * only that single property.
     *
     * @param deliveryMode If [UniqueOnly], when this MvRxView goes from a stopped to start lifecycle a state value
     * will only be emitted if the state changed. This is useful for transient views that should only
     * be shown once (toasts, poptarts), or logging. Most other views should use false, as when a view is destroyed
     * and recreated the previous state is necessary to recreate the view.
     *
     * Use [uniqueOnly] to automatically create a [UniqueOnly] mode with a unique id for this view.
     *
     * Default: [RedeliverOnStart].
     */
    fun <S : MvRxState, A> BaseMvRxViewModel<S>.selectSubscribe(
        prop1: KProperty1<S, A>,
        deliveryMode: DeliveryMode = RedeliverOnStart,
        subscriber: (A) -> Unit
    ) = selectSubscribe(this@MvRxView.subscriptionLifecycleOwner, prop1, deliveryMode, subscriber)

    /**
     * Subscribe to changes in an async property. There are optional parameters for onSuccess
     * and onFail which automatically unwrap the value or error.
     *
     * @param deliveryMode If [UniqueOnly], when this MvRxView goes from a stopped to start lifecycle a state value
     * will only be emitted if the state changed. This is useful for transient views that should only
     * be shown once (toasts, poptarts), or logging. Most other views should use false, as when a view is destroyed
     * and recreated the previous state is necessary to recreate the view.
     *
     * Use [uniqueOnly] to automatically create a [UniqueOnly] mode with a unique id for this view.
     *
     * Default: [RedeliverOnStart].
     */
    fun <S : MvRxState, T> BaseMvRxViewModel<S>.asyncSubscribe(
        asyncProp: KProperty1<S, Async<T>>,
        deliveryMode: DeliveryMode = RedeliverOnStart,
        onFail: ((Throwable) -> Unit)? = null,
        onSuccess: ((T) -> Unit)? = null
    ) = asyncSubscribe(this@MvRxView.subscriptionLifecycleOwner, asyncProp, deliveryMode, onFail, onSuccess)

    /**
     * Subscribes to state changes for two properties.
     *
     * @param deliveryMode If [UniqueOnly], when this MvRxView goes from a stopped to start lifecycle a state value
     * will only be emitted if the state changed. This is useful for transient views that should only
     * be shown once (toasts, poptarts), or logging. Most other views should use false, as when a view is destroyed
     * and recreated the previous state is necessary to recreate the view.
     *
     * Use [uniqueOnly] to automatically create a [UniqueOnly] mode with a unique id for this view.
     *
     * Default: [RedeliverOnStart].
     */
    fun <S : MvRxState, A, B> BaseMvRxViewModel<S>.selectSubscribe(
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        deliveryMode: DeliveryMode = RedeliverOnStart,
        subscriber: (A, B) -> Unit
    ) = selectSubscribe(this@MvRxView.subscriptionLifecycleOwner, prop1, prop2, deliveryMode, subscriber)

    /**
     * Subscribes to state changes for three properties.
     *
     * @param deliveryMode If [UniqueOnly], when this MvRxView goes from a stopped to start lifecycle a state value
     * will only be emitted if the state changed. This is useful for transient views that should only
     * be shown once (toasts, poptarts), or logging. Most other views should use false, as when a view is destroyed
     * and recreated the previous state is necessary to recreate the view.
     *
     * Use [uniqueOnly] to automatically create a [UniqueOnly] mode with a unique id for this view.
     *
     * Default: [RedeliverOnStart].
     */
    fun <S : MvRxState, A, B, C> BaseMvRxViewModel<S>.selectSubscribe(
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        deliveryMode: DeliveryMode = RedeliverOnStart,
        subscriber: (A, B, C) -> Unit
    ) = selectSubscribe(this@MvRxView.subscriptionLifecycleOwner, prop1, prop2, prop3, deliveryMode, subscriber)

    /**
     * Subscribes to state changes for four properties.
     *
     * @param deliveryMode If [UniqueOnly], when this MvRxView goes from a stopped to start lifecycle a state value
     * will only be emitted if the state changed. This is useful for transient views that should only
     * be shown once (toasts, poptarts), or logging. Most other views should use false, as when a view is destroyed
     * and recreated the previous state is necessary to recreate the view.
     *
     * Use [uniqueOnly] to automatically create a [UniqueOnly] mode with a unique id for this view.
     *
     * Default: [RedeliverOnStart].
     */
    fun <S : MvRxState, A, B, C, D> BaseMvRxViewModel<S>.selectSubscribe(
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        prop4: KProperty1<S, D>,
        deliveryMode: DeliveryMode = RedeliverOnStart,
        subscriber: (A, B, C, D) -> Unit
    ) = selectSubscribe(this@MvRxView.subscriptionLifecycleOwner, prop1, prop2, prop3, prop4, deliveryMode, subscriber)
}
