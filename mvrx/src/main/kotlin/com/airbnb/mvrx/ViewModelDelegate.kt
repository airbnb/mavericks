package com.airbnb.mvrx

import androidx.fragment.app.Fragment
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

@PublishedApi
internal inline fun <T, reified VM : BaseMvRxViewModel<S>, reified S : MvRxState> provideViewModel(
    existingViewModel: Boolean,
    noinline viewModelProvider: (stateFactory: MvRxStateFactory<VM, S>) -> VM
): ViewModelDelegate<T, VM, S> where T : Fragment, T : MvRxView {
    return object : ViewModelDelegate<T, VM, S>() {

        override operator fun provideDelegate(
            thisRef: T,
            property: KProperty<*>
        ): lifecycleAwareLazy<VM> {
            val providerFactory: ViewModelProviderFactory = MvRx.viewModelProviderFactory

            val plugin =
                providerFactory.createViewModelProvider<VM, S>(S::class, thisRef, property, existingViewModel)
            return plugin.provideViewModel(viewModelProvider)
        }
    }
}

abstract class ViewModelDelegate<T, VM : BaseMvRxViewModel<S>, S : MvRxState> where T : Fragment, T : MvRxView {

    abstract operator fun provideDelegate(
        thisRef: T,
        property: KProperty<*>
    ): lifecycleAwareLazy<VM>
}

interface ViewModelProviderFactory {
    fun <VM : BaseMvRxViewModel<S>, S : MvRxState> createViewModelProvider(
        stateClass: KClass<S>,
        view: MvRxView,
        viewModelProperty: KProperty<*>,
        existingViewModel: Boolean
    ): ViewModelProvider<VM, S>
}

interface ViewModelProvider<VM : BaseMvRxViewModel<S>, S : MvRxState> {
    fun provideViewModel(originalProvider: (stateFactory: MvRxStateFactory<VM, S>) -> VM): lifecycleAwareLazy<VM>
}

class DefaultViewModelProviderFactory : ViewModelProviderFactory {
    override fun <VM : BaseMvRxViewModel<S>, S : MvRxState> createViewModelProvider(
        stateClass: KClass<S>,
        view: MvRxView,
        viewModelProperty: KProperty<*>,
        existingViewModel: Boolean
    ): ViewModelProvider<VM, S> {
        return object : ViewModelProvider<VM, S> {
            override fun provideViewModel(originalProvider: (stateFactory: MvRxStateFactory<VM, S>) -> VM): lifecycleAwareLazy<VM> {
                return lifecycleAwareLazy(view) {
                    originalProvider(RealMvRxStateFactory())
                        .apply { subscribe(view, subscriber = { view.postInvalidate() }) }
                }
            }
        }
    }
}
