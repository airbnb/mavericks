package com.airbnb.mvrx

import androidx.fragment.app.Fragment
import com.airbnb.mvrx.mock.MockBehavior
import com.airbnb.mvrx.mock.MockableStateStore
import com.airbnb.mvrx.mock.mockStateHolder
import com.airbnb.mvrx.mock.mvrxViewModelConfigProvider
import kotlin.reflect.KProperty

@PublishedApi
internal inline fun <T, reified VM : BaseMvRxViewModel<S>, reified S : MvRxState> provideViewModel(
    existingViewModel: Boolean,
    crossinline viewModelProvider: (fragment: T, stateFactory: MvRxStateFactory<VM, S>, isMocked: Boolean) -> VM
): ViewModelDelegate<T, VM, S> where T : Fragment, T : MvRxView {
    return object : ViewModelDelegate<T, VM, S>() {

        override operator fun provideDelegate(
            thisRef: T,
            property: KProperty<*>
        ): lifecycleAwareLazy<VM> {

            return setupViewModel<S>(thisRef, property, existingViewModel) { mockState ->
                viewModelProvider(
                    thisRef,
                    stateFactory(mockState),
                    mockState != null
                )
            }
        }
    }
}

abstract class ViewModelDelegate<T, VM : BaseMvRxViewModel<S>, S : MvRxState> where T : Fragment, T : MvRxView {

    protected val mockBehavior = mvrxViewModelConfigProvider.mockBehavior

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

        // Mocked state will be null if it is being created from mock arguments, and this is not an "existing" view model
        val mockState: S? =
            // TODO: Is this right for ForceMockExistingViewModel? do we not get mocked state for ForceMockExistingViewModel if it isn't an existing view model?
            if (mockBehavior != null && mockBehavior.initialState != MockBehavior.InitialState.None) {
                mockStateHolder.getMockedState(
                    view = view,
                    viewModelProperty = viewModelProperty,
                    existingViewModel = existingViewModel,
                    stateClass = State::class.java,
                    forceMockExistingViewModel = mockBehavior.initialState == MockBehavior.InitialState.ForceMockExistingViewModel
                )
            } else {
                null
            }

        return lifecycleAwareLazy(view) {
            mvrxViewModelConfigProvider.withMockBehavior(mockBehavior) {
                viewModelProvider(mockState)
                    .apply { subscribe(view, subscriber = { view.postInvalidate() }) }
                    .also { vm ->
                        if (mockState != null && mockBehavior?.initialState == MockBehavior.InitialState.Full) {
                            // Custom viewmodel factories can override initial state, so we also force state from the viewmodel
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
            if (mockState != null) {
                // If a view is being mocked then one of its view models may depend on another,
                // in which case the dependent needs to be initialized after the VM it depends on.
                // Tracking all view model delegates created for a view allows us to for
                // initialize existing view models first, since Fragment view models
                // may depend on existing view models.
                mockStateHolder.addViewModelDelegate(
                    fragment = view,
                    existingViewModel = existingViewModel,
                    viewModelProperty = viewModelProperty,
                    viewModelDelegate = viewModelDelegate
                )
            }
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