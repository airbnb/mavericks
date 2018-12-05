package com.airbnb.mvrx

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity

/**
 * Implement this in the companion object of a MvRxViewModel if your ViewModel needs more dependencies than just initial state.
 * If all you need is initial state, you don't need to implement this at all.
 */
interface MvRxViewModelFactory<S : MvRxState> {
    /**
     * This will be called when your ViewModel needs to created. This *needs* to be annotated with [JvmStatic].
     * @param state: The initial state for your ViewModel. This will be populated from activity persisted state.
     */
    fun create(activity: FragmentActivity, state: S): BaseMvRxViewModel<S>
}

/**
 * Implement this in the companion object of a MvRxViewModel if your ViewModel needs more dependencies than just initial state.
 * If you only need the [FragmentActivity] then you can use the [MvRxViewModelFactory].
 * If all you need is initial state, you don't need to implement this at all.
 */
interface MvRxFragmentViewModelFactory<S : MvRxState> {
    /**
     * This will be called when your ViewModel needs to created. This *needs* to be annotated with [JvmStatic].
     * @param state: The initial state for your ViewModel. This will be populated from fragment args and persisted state.
     */
    fun create(fragment: Fragment, state: S): BaseMvRxViewModel<S>
}
