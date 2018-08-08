package com.airbnb.mvrx

/**
 * A Fragment or Activity that will contain MvRxViewModels must implement this to achieve proper state [PersistState]
 * restoration.
 */
interface MvRxViewModelStoreOwner {
    val mvrxViewModelStore: MvRxViewModelStore
}