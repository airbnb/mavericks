package com.airbnb.mvrx

import android.arch.lifecycle.LifecycleOwner
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlin.reflect.KProperty1

/**
 * Implement this in your MvRx capable Fragment.
 */
interface MvRxView : MvRxViewModelStoreOwner, LifecycleOwner {
    /**
     * Override this to handle any state changes from MvRxViewModels created through MvRx Fragment delegates.
     */
    fun invalidate()

    /**
     * Subscribes to all state updates for the given viewModel.
     *
     * Use shouldUpdate if you only want to subscribe to a subset of all updates. There are some standard ones in ShouldUpdateHelpers.
     */
    fun <S : MvRxState> BaseMvRxViewModel<S>.subscribe(
            subscriber: ((S) -> Unit)? = null
    ) = subscribe(this@MvRxView, subscriber ?: { invalidate() })

    /**
     * Subscribes to state changes for only a specific property and calls the subscribe with
     * only that single property.
     */
    fun <S : MvRxState, A> BaseMvRxViewModel<S>.selectSubscribe(
            prop1: KProperty1<S, A>,
            subscriber: (A) -> Unit
    ) = selectSubscribe(this@MvRxView, prop1, subscriber)

    /**
     * Subscribe to changes in an async property. There are optional parameters for onSuccess
     * and onFail which automatically unwrap the value or error.
     */
    fun <S : MvRxState, T> BaseMvRxViewModel<S>.asyncSubscribe(
        asyncProp: KProperty1<S, Async<T>>,
        onFail: ((Throwable) -> Unit)? = null,
        onSuccess: ((T) -> Unit)? = null
    ) = asyncSubscribe(this@MvRxView, asyncProp, onFail, onSuccess)

    /**
     * Subscribes to state changes for two properties.
     */
    fun <S : MvRxState, A, B> BaseMvRxViewModel<S>.selectSubscribe(
            prop1: KProperty1<S, A>,
            prop2: KProperty1<S, B>,
            subscriber: (A, B) -> Unit
    ) = selectSubscribe(this@MvRxView, prop1, prop2, subscriber)

    /**
     * Subscribes to state changes for three properties.
     */
    fun <S : MvRxState, A, B, C> BaseMvRxViewModel<S>.selectSubscribe(
            prop1: KProperty1<S, A>,
            prop2: KProperty1<S, B>,
            prop3: KProperty1<S, C>,
            subscriber: (A, B, C) -> Unit
    ) = selectSubscribe(this@MvRxView, prop1, prop2, prop3, subscriber)

    /**
     * Subscribes to state changes for four properties.
     */
    fun <S : MvRxState, A, B, C, D> BaseMvRxViewModel<S>.selectSubscribe(
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        prop4: KProperty1<S, D>,
        subscriber: (A, B, C, D) -> Unit
    ) = selectSubscribe(this@MvRxView, prop1, prop2, prop3, prop4, subscriber)
}