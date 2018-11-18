package com.airbnb.mvrx

import android.arch.lifecycle.ViewModelProvider

/**
 * A Fragment or Activity that will contain MvRxViewModels must implement this.
 * It can be implemented easily with the help of Dagger2.
 */
interface ViewModelFactoryOwner {
    val viewModelFactory: ViewModelProvider.Factory
}