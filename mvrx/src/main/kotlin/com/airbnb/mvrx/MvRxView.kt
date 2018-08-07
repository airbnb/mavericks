package com.airbnb.mvrx

/**
 * Implement this in your MvRx capable Fragment.
 */
interface MvRxView : MvRxViewModelStoreOwner {
    /**
     * Override this to handle any state changes from MvRxViewModels created through MvRx Fragment delegates.
     */
    fun invalidate()
    /**
     * Override this to prevent any invalidate calls before certain conditions.
     */
    fun readyToInvalidate(): Boolean
}