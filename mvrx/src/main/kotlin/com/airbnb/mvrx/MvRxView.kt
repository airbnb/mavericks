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
     * @param shouldUpdate is an optional observer that you can implement to filter whether or not
     *                     your subscriber gets called. It will be given the old state and new state
     *                     and should return whether or not to call the subscriber. It will initially
     *                     be called with null as the old state to determine whether or not to
     *                     deliver the initial state.
     *                     MvRx comes with some shouldUpdate helpers such as onSuccess, onFail, and propertyWhitelist.
     */
    fun <S : MvRxState> BaseMvRxViewModel<S>.subscribe(
            shouldUpdate: ((S?, S) -> Boolean)? = null,
            observerScheduler: Scheduler = AndroidSchedulers.mainThread(),
            subscriber: ((S) -> Unit)? = null
    ) = subscribe(this@MvRxView, shouldUpdate, observerScheduler, subscriber ?: { invalidate() })

    /**
     * Subscribes to all state updates for the given viewModel. The subscriber will receive the previous state and the new state.
     *
     * @param shouldUpdate is an optional observer that you can implement to filter whether or not
     *                     your subscriber gets called. It will be given the old state and new state
     *                     and should return whether or not to call the subscriber. It will initially
     *                     be called with null as the old state to determine whether or not to
     *                     deliver the initial state.
     *                     MvRx comes with some shouldUpdate helpers such as onSuccess, onFail, and propertyWhitelist.
     */
    fun <S : MvRxState> BaseMvRxViewModel<S>.subscribeWithHistory(
            shouldUpdate: ((S?, S) -> Boolean)? = null,
            observerScheduler: Scheduler = AndroidSchedulers.mainThread(),
            subscriber: (S?, S) -> Unit
    ) = subscribeWithHistory(this@MvRxView, shouldUpdate, observerScheduler, subscriber)

    /**
     * Subscribes to state changes for only a specific property and calls the subscribe with
     * only that single property.
     */
    fun <S : MvRxState, A> BaseMvRxViewModel<S>.selectSubscribe(
            prop1: KProperty1<S, A>,
            observerScheduler: Scheduler = AndroidSchedulers.mainThread(),
            subscriber: (A) -> Unit
    ) = subscribe(this@MvRxView, propertyWhitelist(prop1), observerScheduler) {
        subscriber(prop1.get(it))
    }

    /**
     * Subscribes to state changes for two specific properties and calls the subscribe with
     * both properties.
     */
    fun <S : MvRxState, A, B> BaseMvRxViewModel<S>.selectSubscribe(
            prop1: KProperty1<S, A>,
            prop2: KProperty1<S, B>,
            observerScheduler: Scheduler = AndroidSchedulers.mainThread(),
            subscriber: (A, B) -> Unit
    ) = subscribe(this@MvRxView, propertyWhitelist(prop1), observerScheduler) {
        subscriber(prop1.get(it), prop2.get(it))
    }

    /**
     * Subscribes to state changes for two specific properties and calls the subscribe with
     * both properties.
     */
    fun <S : MvRxState, A, B, C> BaseMvRxViewModel<S>.selectSubscribe(
            prop1: KProperty1<S, A>,
            prop2: KProperty1<S, B>,
            prop3: KProperty1<S, C>,
            observerScheduler: Scheduler = AndroidSchedulers.mainThread(),
            subscriber: (A, B, C) -> Unit
    ) = subscribe(this@MvRxView, propertyWhitelist(prop1), observerScheduler) {
        subscriber(prop1.get(it), prop2.get(it), prop3.get(it))
    }
}