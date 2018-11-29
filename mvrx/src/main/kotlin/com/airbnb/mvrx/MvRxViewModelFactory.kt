package com.airbnb.mvrx

import androidx.fragment.app.FragmentActivity

/**
 * Implement this in the companion object of a MvRxViewModel if your ViewModel needs more dependencies than just initial state.
 * If all you need is initial state, you don't need to implement this at all.
 */
interface MvRxViewModelFactory<S : MvRxState> {
    /**
     * This will be called when your ViewModel needs to created. This *needs* to be annotated with [JvmStatic].
     * @param state: The initial state for your ViewModel. This will be populated from fragment / activity args
     * and persisted state.
     */
    fun create(activity: FragmentActivity, state: S): BaseMvRxViewModel<S>
}