package com.airbnb.mvrx

import androidx.fragment.app.Fragment
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

@PublishedApi
internal inline fun <T, reified VM : BaseMvRxViewModel<S>, reified S : MvRxState> viewModelDelegateProvider(
    existingViewModel: Boolean,
    noinline viewModelProvider: (stateFactory: MvRxStateFactory<VM, S>) -> VM
): DelegateProvider<T, VM> where T : Fragment, T : MvRxView {
    return object : DelegateProvider<T, VM>() {

        override operator fun provideDelegate(
            thisRef: T,
            property: KProperty<*>
        ): Lazy<VM> {
            val delegateFactory: ViewModelDelegateFactory = MvRx.viewModelDelegateFactory

            val viewModelDelegateFactory = delegateFactory.createViewModelDelegate<VM, S>(
                stateClass = S::class,
                view = thisRef,
                viewModelProperty = property,
                existingViewModel = existingViewModel
            )
            return viewModelDelegateFactory.createLazyViewModel(viewModelProvider)
        }
    }
}

@PublishedApi
internal abstract class DelegateProvider<T, R>  {

    abstract operator fun provideDelegate(
        thisRef: T,
        property: KProperty<*>
    ): Lazy<R>
}

interface ViewModelDelegateFactory {
    fun <VM : BaseMvRxViewModel<S>, S : MvRxState> createViewModelDelegate(
        stateClass: KClass<S>,
        view: MvRxView,
        viewModelProperty: KProperty<*>,
        existingViewModel: Boolean
    ): GlobalViewModelFactory<VM, S>
}

interface GlobalViewModelFactory<VM : BaseMvRxViewModel<S>, S : MvRxState> {
    fun createLazyViewModel(originalProvider: (stateFactory: MvRxStateFactory<VM, S>) -> VM): Lazy<VM>
}

class DefaultGlobalViewModelFactory : ViewModelDelegateFactory {
    override fun <VM : BaseMvRxViewModel<S>, S : MvRxState> createViewModelDelegate(
        stateClass: KClass<S>,
        view: MvRxView,
        viewModelProperty: KProperty<*>,
        existingViewModel: Boolean
    ): GlobalViewModelFactory<VM, S> {
        return object : GlobalViewModelFactory<VM, S> {
            override fun createLazyViewModel(originalProvider: (stateFactory: MvRxStateFactory<VM, S>) -> VM): lifecycleAwareLazy<VM> {
                return lifecycleAwareLazy(view) {
                    originalProvider(RealMvRxStateFactory())
                        .apply { subscribe(view, subscriber = { view.postInvalidate() }) }
                }
            }
        }
    }
}
