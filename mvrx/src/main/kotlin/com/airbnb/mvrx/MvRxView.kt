package com.airbnb.mvrx

import android.arch.lifecycle.LifecycleOwner
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Implement this in your MvRx capable Fragment.
 */
interface MvRxView : MvRxViewModelStoreOwner, LifecycleOwner {
    /**
     * Override this to handle any state changes from MvRxViewModels created through MvRx Fragment delegates.
     */
    fun invalidate()
    /**
     * Override this to prevent any invalidate calls before certain conditions.
     */
    fun readyToInvalidate(): Boolean


    /**
     * Subscribes to all state updates for the given viewModel.
     *
     * Use shouldUpdate if you only want to subscribe to a subset of all updates. There are some standard ones in ShouldUpdateHelpers.
     */
    fun <S : MvRxState> BaseMvRxViewModel<S>.subscribe(
            shouldUpdate: ((S, S) -> Boolean)? = null,
            observerScheduler: Scheduler = AndroidSchedulers.mainThread(),
            subscriber: ((S) -> Unit)? = null
    ) = subscribe(this@MvRxView, shouldUpdate, observerScheduler, subscriber ?: { if (readyToInvalidate()) invalidate() })

    /**
     * Subscribes to all state updates for the given viewModel. The subscriber will receive the previous state and the new state.
     *
     * Use shouldUpdate if you only want to subscribe to a subset of all updates. There are some standard ones in ShouldUpdateHelpers.
     */
    fun <S : MvRxState> BaseMvRxViewModel<S>.subscribeWithHistory(
            shouldUpdate: ((S, S) -> Boolean)? = null,
            observerScheduler: Scheduler = AndroidSchedulers.mainThread(),
            subscriber: (S, S) -> Unit
    ) = subscribeWithHistory(this@MvRxView, shouldUpdate, observerScheduler, subscriber)
}