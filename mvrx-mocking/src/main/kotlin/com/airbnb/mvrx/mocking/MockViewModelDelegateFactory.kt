package com.airbnb.mvrx.mocking

import androidx.fragment.app.Fragment
import com.airbnb.mvrx.ActivityViewModelContext
import com.airbnb.mvrx.Mavericks
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksStateFactory
import com.airbnb.mvrx.MavericksViewModelProvider
import com.airbnb.mvrx.RealMavericksStateFactory
import com.airbnb.mvrx.ViewModelContext
import com.airbnb.mvrx.ViewModelDelegateFactory
import com.airbnb.mvrx.ViewModelDoesNotExistException
import com.airbnb.mvrx.lifecycleAwareLazy
import com.airbnb.mvrx._internal
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * This delegate factory creates ViewModels that are optionally mockable, as configured by
 * [MockMavericksViewModelConfigFactory.mockBehavior].
 *
 * If a mock behavior is enabled, then when a ViewModel is created this will look for a mock state
 * in [MockableMavericks.mockStateHolder], and if one exists it will be forced onto the ViewModel.
 */
class MockViewModelDelegateFactory(
    val configFactory: MockMavericksViewModelConfigFactory
) : ViewModelDelegateFactory {

    override fun <S : MavericksState, T, VM : MavericksViewModel<S>> createLazyViewModel(
        fragment: T,
        viewModelProperty: KProperty<*>,
        viewModelClass: KClass<VM>,
        keyFactory: () -> String,
        stateClass: KClass<S>,
        existingViewModel: Boolean,
        viewModelProvider: (stateFactory: MavericksStateFactory<VM, S>) -> VM
    ): Lazy<VM> where T : Fragment, T : MavericksView {
        check(configFactory == Mavericks.viewModelConfigFactory) {
            "Config factory provided in constructor is not the same one as initialized on Mavericks object."
        }

        // We lock  in the mockBehavior at the time that the Fragment is created (which is when the
        // delegate provider is created). Using the mockbehavior at this time is necessary since it allows
        // consistency in knowing what mock behavior a Fragment will get. If we used the mock behavior
        // at the time when the viewmodel is created it would be some point in the future (because it is lazy)
        // and that is harder to determine and control for.
        val mockBehavior = configFactory.mockBehavior

        // Accessing mock state is done lazily because in the case of existing view models
        // no mock state may have been set and trying to access it would throw an error.
        // So, we only look for mock state when we know it is needed.
        val mockState: S? by lazy {
            getMockState(fragment, mockBehavior, viewModelProperty, existingViewModel, stateClass)
        }

        return lifecycleAwareLazy(fragment) {

            configFactory.withMockBehavior(mockBehavior) {
                val viewModel: VM = getMockedViewModel(
                    existingViewModel,
                    viewModelProvider,
                    fragment,
                    keyFactory,
                    viewModelClass,
                    stateClass,
                    mockState,
                    mockBehavior
                )

                viewModel.apply {
                    if (mockBehavior.subscribeViewToStateUpdates) {
                        _internal(fragment, action = { fragment.postInvalidate() })
                    }
                }.also { vm ->
                    if (mockState != null && mockBehavior.initialStateMocking == MockBehavior.InitialStateMocking.Full) {
                        // Custom viewmodel factories can override initial state, so we also force state on the viewmodel
                        // to be the expected mocked value after the ViewModel has been created.

                        val stateStore =
                            vm.config.stateStore as? MockableStateStore
                                ?: error("Expected a mockable state store for 'Full' mock behavior.")

                        require(stateStore.mockBehavior.stateStoreBehavior == MockBehavior.StateStoreBehavior.Scriptable) {
                            "Full mock state requires that the state store be set to scriptable to " +
                                "guarantee that state is frozen on the mock and not allowed to be changed by the view."
                        }

                        stateStore.next(mockState!!)
                    }
                }
            }
        }.also { viewModelDelegate ->
            if (fragment is MockableMavericksView) {
                // If a view is being mocked then one of its view models may depend on another,
                // in which case the dependent needs to be initialized after the VM it depends on.
                // Tracking all view model delegates created for a view allows us to
                // initialize existing view models first, since Fragment view models
                // may depend on existing view models.
                MockableMavericks.mockStateHolder.addViewModelDelegate(
                    view = fragment,
                    existingViewModel = existingViewModel,
                    viewModelProperty = viewModelProperty,
                    viewModelDelegate = viewModelDelegate
                )
            }
        }
    }

    private fun <S : MavericksState, T, VM : MavericksViewModel<S>> getMockedViewModel(
        existingViewModel: Boolean,
        viewModelProvider: (stateFactory: MavericksStateFactory<VM, S>) -> VM,
        fragment: T,
        keyFactory: () -> String,
        viewModelClass: KClass<VM>,
        stateClass: KClass<S>,
        mockState: S?,
        mockBehavior: MockBehavior
    ): VM where T : Fragment, T : MavericksView {
        return if (existingViewModel && mockBehavior.initialStateMocking != MockBehavior.InitialStateMocking.None) {
            // When the fragment uses "existingViewModel" normally it should always exist.
            // However, when we are mocking initial state then it is equally valid for it to already exist
            // or not exist.
            // If it does exist we can look it up and return it, and  if it does not exist
            // we can create it like "activityViewModel" behavior would.
            try {
                viewModelProvider(object : MavericksStateFactory<VM, S> {
                    override fun createInitialState(
                        viewModelClass: Class<out VM>,
                        stateClass: Class<out S>,
                        viewModelContext: ViewModelContext,
                        stateRestorer: (S) -> S
                    ): S {
                        //  Throwing this indicates to us that the view model didn't exist,
                        // since the factory will only be invoked when creating a new view model.
                        throw ViewModelDoesNotExistException(
                            viewModelClass,
                            ActivityViewModelContext(fragment.requireActivity(), null),
                            keyFactory()
                        )
                    }
                })
            } catch (e: ViewModelDoesNotExistException) {
                // When existing view models don't exist it is normally an error, but since
                // we are mocking them we just create a new view  model with the mocked state.
                // This copies the behavior of "activityViewModel".
                MavericksViewModelProvider.get(
                    viewModelClass = viewModelClass.java,
                    stateClass = stateClass.java,
                    viewModelContext = ActivityViewModelContext(
                        activity = fragment.requireActivity(),
                        args = fragment._fragmentArgsProvider()
                    ),
                    key = keyFactory(),
                    initialStateFactory = stateFactory(mockState)
                )
            }
        } else {
            viewModelProvider(stateFactory(mockState))
        }
    }

    @Suppress("FunctionName")
    private fun <T : Fragment> T._fragmentArgsProvider(): Any? = arguments?.get(Mavericks.KEY_ARG)

    private fun <S : MavericksState, T : MavericksView> getMockState(
        fragment: T,
        mockBehavior: MockBehavior,
        viewModelProperty: KProperty<*>,
        existingViewModel: Boolean,
        stateClass: KClass<S>
    ): S? {
        return if (fragment is MockableMavericksView && mockBehavior.initialStateMocking != MockBehavior.InitialStateMocking.None) {
            MockableMavericks.mockStateHolder.getMockedState(
                view = fragment,
                viewModelProperty = viewModelProperty,
                existingViewModel = existingViewModel,
                stateClass = stateClass.java,
                forceMockExistingViewModel = mockBehavior.initialStateMocking == MockBehavior.InitialStateMocking.ForceMockExistingViewModel
            )
        } else {
            null
        }
    }

    private fun <S : MavericksState, VM : MavericksViewModel<S>> stateFactory(
        mockState: S?
    ): MavericksStateFactory<VM, S> {
        return if (mockState == null) {
            RealMavericksStateFactory()
        } else {
            object : MavericksStateFactory<VM, S> {
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
