package com.airbnb.mvrx.mock

import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.MvRx
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.MvRxStateFactory
import com.airbnb.mvrx.MvRxView
import com.airbnb.mvrx.RealMvRxStateFactory
import com.airbnb.mvrx.ViewModelContext
import com.airbnb.mvrx.ViewModelProvider
import com.airbnb.mvrx.lifecycleAwareLazy
import com.airbnb.mvrx.mock.printer.MvRxMockPrinter
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class MockViewModelDelegatePlugin<VM : BaseMvRxViewModel<S>, S : MvRxState>(
    val stateClass: KClass<S>,
    val view: MvRxView,
    val viewModelProperty: KProperty<*>,
    val existingViewModel: Boolean
) : ViewModelProvider<VM, S> {
    // We lock  in the mockBehavior at the time that the Fragment is created (which is when the
    // delegate provider is created). Using the mockbehavior at this time is necessary since it allows
    // consistency in knowing what mock behavior a Fragment will get. If we used the mock behavior
    // at the time when the viewmodel is created it would be some point in the future that is harder
    // to determine and control for.
    private val mockBehavior = MvRx.viewModelConfigProvider.mockBehavior

    override fun provideViewModel(originalProvider: (stateFactory: MvRxStateFactory<VM, S>) -> VM): lifecycleAwareLazy<VM> {
        return lifecycleAwareLazy(view) {
            val mockState: S? = getMockedState()

            MvRx.viewModelConfigProvider.withMockBehavior(
                mockBehavior
            ) {
                originalProvider(stateFactory(mockState))
                    .apply { subscribe(view, subscriber = { view.postInvalidate() }) }
                    .also { vm ->
                        if (mockState != null && mockBehavior?.initialState == MockBehavior.InitialState.Full) {
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
            if (mockBehavior != null) {
                // If a view is being mocked then one of its view models may depend on another,
                // in which case the dependent needs to be initialized after the VM it depends on.
                // Tracking all view model delegates created for a view allows us to
                // initialize existing view models first, since Fragment view models
                // may depend on existing view models.
                MvRxMocks.mockStateHolder.addViewModelDelegate(
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
    private fun getMockedState(): S? {
        return if (mockBehavior != null && mockBehavior.initialState != MockBehavior.InitialState.None) {
            MvRxMocks.mockStateHolder.getMockedState(
                view = view,
                viewModelProperty = viewModelProperty,
                existingViewModel = existingViewModel,
                stateClass = stateClass.java,
                forceMockExistingViewModel = mockBehavior.initialState == MockBehavior.InitialState.ForceMockExistingViewModel
            )
        } else {
            null
        }
    }

    private fun stateFactory(
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