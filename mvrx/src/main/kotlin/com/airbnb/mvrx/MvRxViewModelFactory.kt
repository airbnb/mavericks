package com.airbnb.mvrx

import android.support.v4.app.FragmentActivity

/**
 * Implement this in the companion object of a [MvRxViewModel] if your ViewModel needs more dependencies than just initial state.
 * If all you need is initial state, you don't need to implement this at all.
 *
 * If you need additional or shared dependencies, use dagger and get your dagger subcomponent using
 * the extension method `activity.getOrCreateSubcomponent(YourGraph::yourBuilder)`.
 */
interface MvRxViewModelFactory<S : MvRxState> {
    fun create(activity: FragmentActivity, state: S): BaseMvRxViewModel<S>
}