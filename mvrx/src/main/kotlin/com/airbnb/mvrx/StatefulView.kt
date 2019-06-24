package com.airbnb.mvrx

import android.view.View
import io.reactivex.disposables.Disposable

/**
 * Implement this in a custom view to expose [fragmentViewModel] and [activityViewModel] in a custom view. Similar to usage in Fragments,
 * this will create a view-lifecycle-aware subscription.
 * For [fragmentViewModel], this will walk up the view hierarchy until it finds a View that is the root view for a Fragment.
 * [StatefulView] uses [onInvalidate] instead of `invalidate()` because `invalidate()` is already a function on View. It is also
 * not called [MvRxView] because the name was already taken.
 */
interface StatefulView {
    // TODO: remove posting invalidate.
    fun postOnInvalidate() {
        MvRxInvalidator.post(this)
    }

    fun onInvalidate()

    /**
     * TODO: Implement this
     * TODO: Ensure this crashes if subscriptions happen outside of it.
     */
    fun setupSubscriptions(): List<Disposable> = emptyList()

    fun <T> T.selectSubscribe() where T : View, T : StatefulView {
    }
}