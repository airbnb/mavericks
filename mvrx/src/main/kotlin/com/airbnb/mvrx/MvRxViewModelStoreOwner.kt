package com.airbnb.mvrx

/**
 * A Fragment or Activity that will contain MvRxViewModels must implement this to achieve proper state [PersistState]
 * resotration.
 */
interface MvRxViewModelStoreOwner {
    val mvrxViewModelStore: MvRxViewModelStore
}