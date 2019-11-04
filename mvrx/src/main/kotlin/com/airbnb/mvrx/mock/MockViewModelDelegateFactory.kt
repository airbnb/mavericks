package com.airbnb.mvrx.mock

import androidx.fragment.app.Fragment
import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.MvRx
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.MvRxStateFactory
import com.airbnb.mvrx.MvRxView
import com.airbnb.mvrx.RealMvRxStateFactory
import com.airbnb.mvrx.ViewModelContext
import com.airbnb.mvrx.ViewModelDelegateFactory
import com.airbnb.mvrx.lifecycleAwareLazy
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * This delegate factory creates ViewModels that are optionally mockable, as configured by
 * [MockMvRxViewModelConfigFactory.mockBehavior].
 *
 * If a mock behavior is enabled, then when a ViewModel is created this will look for a mock state
 * in [MvRxMocks.mockStateHolder], and if one exists it will be forced onto the ViewModel.
 */
class MockViewModelDelegateFactory(
    val configFactory: MockMvRxViewModelConfigFactory
) : ViewModelDelegateFactory {

    // We lock  in the mockBehavior at the time that the Fragment is created (which is when the
    // delegate provider is created). Using the mockbehavior at this time is necessary since it allows
    // consistency in knowing what mock behavior a Fragment will get. If we used the mock behavior
    // at the time when the viewmodel is created it would be some point in the future that is harder
    // to determine and control for.
    private val mockBehavior = configFactory.mockBehavior

    override fun <S : MvRxState, T : Fragment, VM : BaseMvRxViewModel<S>> createLazyViewModel(
        fragment: T,
        viewModelProperty: KProperty<*>,
        stateClass: KClass<S>,
        existingViewModel: Boolean,
        viewModelProvider: (stateFactory: MvRxStateFactory<VM, S>) -> VM
    ): Lazy<VM> where T : MvRxView {
        check(configFactory == MvRx.viewModelConfigFactory) {
            "Config factory provided in constructor is not the same one as installed on MvRx object."
        }

        return lifecycleAwareLazy(fragment) {
            val mockState: S? =
                if (fragment is MockableMvRxView && mockBehavior.initialState != MockBehavior.InitialState.None) {
                    MvRxMocks.mockStateHolder.getMockedState(
                        view = fragment,
                        viewModelProperty = viewModelProperty,
                        existingViewModel = existingViewModel,
                        stateClass = stateClass.java,
                        forceMockExistingViewModel = mockBehavior.initialState == MockBehavior.InitialState.ForceMockExistingViewModel
                    )
                } else {
                    null
                }

            configFactory.withMockBehavior(
                mockBehavior
            ) {
                viewModelProvider(stateFactory(mockState))
                    .apply { subscribe(fragment, subscriber = { fragment.postInvalidate() }) }
                    .also { vm ->
                        if (mockState != null && mockBehavior.initialState == MockBehavior.InitialState.Full) {
                            // Custom viewmodel factories can override initial state, so we also force state on the viewmodel
                            // to be the expected mocked value after the ViewModel has been created.

                            val stateStore =
                                vm.config.stateStore as? MockableStateStore
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
            if (fragment is MockableMvRxView) {
                // If a view is being mocked then one of its view models may depend on another,
                // in which case the dependent needs to be initialized after the VM it depends on.
                // Tracking all view model delegates created for a view allows us to
                // initialize existing view models first, since Fragment view models
                // may depend on existing view models.
                MvRxMocks.mockStateHolder.addViewModelDelegate(
                    view = fragment,
                    existingViewModel = existingViewModel,
                    viewModelProperty = viewModelProperty,
                    viewModelDelegate = viewModelDelegate
                )
            }

        }
    }


    private fun <S : MvRxState, VM : BaseMvRxViewModel<S>> stateFactory(
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