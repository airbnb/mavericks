package com.airbnb.mvrx

import androidx.fragment.app.Fragment
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * Creates an object that provides a Lazy ViewModel for use in Fragments.
 */
@PublishedApi
internal inline fun <T, reified VM : BaseMvRxViewModel<S>, reified S : MvRxState> viewModelDelegateProvider(
    viewModelClass: KClass<VM>,
    crossinline keyFactory: () -> String,
    existingViewModel: Boolean,
    noinline viewModelProvider: (stateFactory: MvRxStateFactory<VM, S>) -> VM
): MvRxDelegateProvider<T, VM> where T : Fragment, T : MvRxView {
    return object : MvRxDelegateProvider<T, VM>() {


        override operator fun provideDelegate(
            thisRef: T,
            property: KProperty<*>
        ): Lazy<VM> {
            return MvRx.viewModelDelegateFactory.createLazyViewModel(
                stateClass = S::class,
                fragment = thisRef,
                viewModelProperty = property,
                viewModelClass = viewModelClass,
                keyFactory = { keyFactory() },
                existingViewModel = existingViewModel,
                viewModelProvider = viewModelProvider
            )
        }
    }
}

abstract class MvRxDelegateProvider<T, R> {

    abstract operator fun provideDelegate(
        thisRef: T,
        property: KProperty<*>
    ): Lazy<R>
}

/**
 * This is invoked each time a Fragment accesses a ViewModel via the MvRx extension functions
 * (eg [fragmentViewModel], [activityViewModel], [existingViewModel]).
 *
 * It allows global callbacks for when a view model is instantiated, with control over how the view model state
 * should be instantiated.
 */
interface ViewModelDelegateFactory {
    /**
     * Create a Lazy ViewModel for the given Fragment.
     *
     * @param existingViewModel If true the view model is expected to already exist, so a new
     * one should not need to be created.
     * @param viewModelProvider This function should be used to actually do the work of creating
     * the viewmodel. It knows how to configure the viewmodel, and just needs to be provided with
     * a state factory.
     */
    fun <S : MvRxState, T, VM : BaseMvRxViewModel<S>> createLazyViewModel(
        fragment: T,
        viewModelProperty: KProperty<*>,
        viewModelClass: KClass<VM>,
        keyFactory: () -> String,
        stateClass: KClass<S>,
        existingViewModel: Boolean,
        viewModelProvider: (stateFactory: MvRxStateFactory<VM, S>) -> VM
    ): Lazy<VM> where T : Fragment, T : MvRxView
}

/**
 * Creates ViewModels that are wrapped with a [lifecycleAwareLazy] so that the ViewModel
 * is automatically created when the Fragment is started (if it is not accessed before then).
 *
 * ViewModels are created with a [RealMvRxStateStore].
 *
 * The Fragment is subscribed to all changes to the ViewModel, so that [MvRxView.postInvalidate] is
 * called on each State change. This allows the Fragment view to be automatically invalidated,
 * only while the Fragment is in the STARTED lifecycle state.
 */
class DefaultViewModelDelegateFactory : ViewModelDelegateFactory {
    override fun <S : MvRxState, T, VM : BaseMvRxViewModel<S>> createLazyViewModel(
        fragment: T,
        viewModelProperty: KProperty<*>,
        viewModelClass: KClass<VM>,
        keyFactory: () -> String,
        stateClass: KClass<S>,
        existingViewModel: Boolean,
        viewModelProvider: (stateFactory: MvRxStateFactory<VM, S>) -> VM
    ): Lazy<VM> where T : MvRxView, T : Fragment {
        return lifecycleAwareLazy(fragment) {
            viewModelProvider(RealMvRxStateFactory())
                .apply { subscribe(fragment, subscriber = { fragment.postInvalidate() }) }
        }
    }

}
