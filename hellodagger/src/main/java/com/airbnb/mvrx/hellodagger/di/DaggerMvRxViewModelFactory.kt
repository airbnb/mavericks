package com.airbnb.mvrx.hellodagger.di

import androidx.fragment.app.FragmentActivity
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.airbnb.mvrx.hellodagger.appComponent
import com.airbnb.mvrx.hellodagger.base.BaseViewModel

/**
 * A [MvRxViewModelFactory] which makes it easy to create instances of a ViewModel
 * using its AssistedInject Factory. This class should be implemented by the companion object
 * of every ViewModel which uses AssistedInject.
 *
 * @param viewModelClass The [Class] of the ViewModel being requested for creation
 *
 * This class accesses the map of [AssistedViewModelFactory]s from [AppComponent] and uses it to
 * retrieve the requested ViewModel's factory class. It then creates an instance of this ViewModel
 * using the retrieved factory and returns it.
 *
 * Example:
 *
 * class MyViewModel @AssistedInject constructor(...): BaseViewModel<MyState>(...) {
 *
 *   @AssistedInject.Factory
 *   interface Factory : AssistedViewModelFactory<MyViewModel, MyState> {
 *     ...
 *   }
 *
 *   companion object : DaggerMvRxViewModelFactory<MyViewModel, MyState>(MyViewModel::class.java)
 *
 * }
 */
abstract class DaggerMvRxViewModelFactory<VM : BaseViewModel<S>, S : MvRxState>(
    private val viewModelClass: Class<out BaseViewModel<S>>
) : MvRxViewModelFactory<VM, S> {

    override fun create(viewModelContext: ViewModelContext, state: S): VM? {
        return createViewModel(viewModelContext.activity, state)
    }

    private fun <VM : BaseViewModel<S>, S : MvRxState> createViewModel(fragmentActivity: FragmentActivity, state: S): VM {
        val viewModelFactoryMap = fragmentActivity.appComponent().viewModelFactories()
        val viewModelFactory = viewModelFactoryMap[viewModelClass]
        @Suppress("UNCHECKED_CAST")
        val castedViewModelFactory = viewModelFactory as? AssistedViewModelFactory<VM, S>
        val viewModel = castedViewModelFactory?.create(state)
        return viewModel as VM
    }
}