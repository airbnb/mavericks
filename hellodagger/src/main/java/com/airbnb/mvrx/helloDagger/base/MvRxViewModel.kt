package com.airbnb.mvrx.helloDagger.base

import androidx.fragment.app.FragmentActivity
import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.helloDagger.BuildConfig
import com.airbnb.mvrx.helloDagger.di.AssistedViewModelFactory

/**
 * Base class for ViewModels.
 *
 * This class sets the 'Debug' mode in a [BaseMvRxViewModel] to the corresponding parameter
 * in the [BuildConfig] class. It also adds the ability to create instances of its subclasses
 * using their AssistedInject Factories. **Each AssistedInject Factory should implement the
 * [AssistedViewModelFactory] interface.**
 */
abstract class MvRxViewModel<S : MvRxState>(initialState: S) : BaseMvRxViewModel<S>(initialState, BuildConfig.DEBUG) {

    companion object {
        inline fun <reified VM : MvRxViewModel<S>, reified S : MvRxState> createViewModel(
                fragmentActivity: FragmentActivity,
                state: S
        ): VM {
            val activity = fragmentActivity as MvRxActivity
            val viewModelFactory = activity.viewModelFactoryMap[VM::class.java]
            @Suppress("UNCHECKED_CAST")
            val castedViewModelFactory = viewModelFactory as? AssistedViewModelFactory<VM, S>
            val viewModel = castedViewModelFactory?.create(state)
            return viewModel as VM
        }
    }
}