package com.airbnb.mvrx

import androidx.fragment.app.Fragment
import com.airbnb.mvrx.mock.MockBehavior
import com.airbnb.mvrx.mock.MockableStateStore
import kotlin.reflect.KProperty

@PublishedApi
internal inline fun <T, reified VM : BaseMvRxViewModel<S>, reified S : MvRxState> provideViewModel(
    existingViewModel: Boolean,
    crossinline viewModelProvider: (fragment: T, stateFactory: MvRxStateFactory<VM, S>) -> VM
): ViewModelDelegate<T, VM, S> where T : Fragment, T : MvRxView {
    return object : ViewModelDelegate<T, VM, S>() {

        override operator fun provideDelegate(
            thisRef: T,
            property: KProperty<*>
        ): lifecycleAwareLazy<VM> {

                return setupViewModel<S>(thisRef, property, existingViewModel) { mockState ->
                viewModelProvider(
                    thisRef,
                    stateFactory(mockState)
                )
            }
        }
    }
}

/**
 * This ViewModel provider checks whether we are creating a mocked ViewModel or not, and handles
 * setup accordingly.
 *
 * In both cases, a subscriber is registered on the ViewModel to invalidate the View whenever state
 * changes.
 *
 * In the mock case, the mock state is looked up from [MvRx.mockStateHolder] and (if it exists)
 * it is forced as the state on the ViewModel.
 */
abstract class ViewModelDelegate<T, VM : BaseMvRxViewModel<S>, S : MvRxState> where T : Fragment, T : MvRxView {

    // We lock  in the mockBehavior at the time that the Fragment is created (which is when the
    // delegate provider is created). Using the mockbehavior at this time is necessary since it allows
    // consistency in knowing what mock behavior a Fragment will get. If we used the mock behavior
    // at the time when the viewmodel is created it would be some point in the future that is harder
    // to determine and control for.
    val mockBehavior = MvRx.viewModelConfigProvider.mockBehavior

    abstract operator fun provideDelegate(
        thisRef: T,
        property: KProperty<*>
    ): lifecycleAwareLazy<VM>

    protected inline fun <reified State : S> setupViewModel(
        view: T,
        viewModelProperty: KProperty<*>,
        existingViewModel: Boolean,
        crossinline viewModelProvider: (mockState: S?) -> VM
    ): lifecycleAwareLazy<VM> {

        return lifecycleAwareLazy(view) {
            val mockState: S? = getMockedState<State>(view, viewModelProperty, existingViewModel)

            MvRx.viewModelConfigProvider.withMockBehavior(mockBehavior) {
                viewModelProvider(mockState)
                    .apply { subscribe(view, subscriber = { view.postInvalidate() }) }
                    .also { vm ->
                        if (mockState != null && mockBehavior?.initialState == MockBehavior.InitialState.Full) {
                            // Custom viewmodel factories can override initial state, so we also force state on the viewmodel
                            // to be the expected mocked value after the ViewModel has been created.

                            val stateStore = vm.config.stateStore as? MockableStateStore
                                ?: error("Expected a mockable state store for 'Full' mock behavior.")

                            require(stateStore.mockBehavior.stateStoreBehavior == MockBehavior.StateStoreBehavior.Scriptable) {
                                "Full mock state requires that the state store be set to scriptable to " +
                                        "guarantee that state is frozen on the mock and not allowed to be changed by the view."
                            }

                            stateStore.next(mockState)
                        }
                    }
            }
        }.also { viewModelDelegate ->
            if (mockBehavior != null) {
                // If a view is being mocked then one of its view models may depend on another,
                // in which case the dependent needs to be initialized after the VM it depends on.
                // Tracking all view model delegates created for a view allows us to
                // initialize existing view models first, since Fragment view models
                // may depend on existing view models.
                MvRx.mockStateHolder.addViewModelDelegate(
                    view = view,
                    existingViewModel = existingViewModel,
                    viewModelProperty = viewModelProperty,
                    viewModelDelegate = viewModelDelegate
                )
            }
        }
    }

    /**
     * If a [MockBehavior] has been set that specifies a mock initial state, then this looks up
     * which mock state was registered to be used for this ViewModel and View.
     *
     * Even if we are expected to mock initial state, the state can be null
     * if it is being created from mock arguments and this is not an "existing" view model.
     */
    @PublishedApi
    internal inline fun <reified State : S> getMockedState(
        view: T,
        viewModelProperty: KProperty<*>,
        existingViewModel: Boolean
    ): S? {
        return if (mockBehavior != null && mockBehavior.initialState != MockBehavior.InitialState.None) {
            MvRx.mockStateHolder.getMockedState(
                view = view,
                viewModelProperty = viewModelProperty,
                existingViewModel = existingViewModel,
                stateClass = State::class.java,
                forceMockExistingViewModel = mockBehavior.initialState == MockBehavior.InitialState.ForceMockExistingViewModel
            )
        } else {
            null
        }
    }

    internal fun stateFactory(
        mockState: S?
    ): MvRxStateFactory<VM, S> {
        return if (mockState == null) {
            RealMvRxStateFactory()
        } else {
            object : MvRxStateFactory<VM, S> {
                override fun createInitialState(
                    viewModelClass: Class<out VM>,
                    stateClass: Class<out S>,
                    viewModelContext: ViewModelContext,
                    stateRestorer: (S) -> S
                ): S {
                    return mockState
                }
            }
        }
    }
}