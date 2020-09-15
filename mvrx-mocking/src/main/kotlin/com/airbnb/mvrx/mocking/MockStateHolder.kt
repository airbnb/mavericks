package com.airbnb.mvrx.mocking

import android.os.Parcelable
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.lifecycleAwareLazy
import kotlin.reflect.KProperty

/**
 * Used to mock the initial state value of a viewmodel.
 */
class MockStateHolder {

    private val stateMap = mutableMapOf<MockableMavericksView, MavericksMock<*, *>>()
    private val delegateInfoMap =
        mutableMapOf<MockableMavericksView, MutableList<ViewModelDelegateInfo<*, *>>>()

    /**
     * Set mock data for a view reference. The mocks will be used to provide initial state
     * when the view models are initialized as the view is started.
     */
    fun <V : MockableMavericksView, A : Parcelable> setMock(
        view: MockableMavericksView,
        mockInfo: MavericksMock<V, A>
    ) {
        stateMap[view] = mockInfo
    }

    /**
     * Clear the stored mock info for the given view. This should be called after the view is done initializing itself with the mock.
     * This should be done to prevent leaking references to the view and its mocks.
     */
    fun clearMock(view: MockableMavericksView) {
        stateMap.remove(view)
        // If the mocked view was just mocked with args and doesn't haven't view models then this will be empty
        delegateInfoMap.remove(view)
    }

    fun clearAllMocks() {
        stateMap.clear()
        delegateInfoMap.clear()
    }

    /**
     * Get the mocked state for the viewmodel on the given view. Returns null if no mocked state has been set - this is valid if a view
     * under mock contains nested views that are not under mock.
     *
     * The mock state is not cleared after being retrieved because if the view has multiple viewmodels they will each need to
     * access this separately.
     *
     * If null is returned it means that the view should be initialized from its arguments.
     * This will only happen if this is not an "existing" view model.
     *
     * @param forceMockExistingViewModel If true, and if [existingViewModel] is true, then we expect that no mock state has been set for this viewmodel
     * and we should instead manually retrieve the default mock state from the View and force that as the mock state to use.
     */
    fun <S : MavericksState> getMockedState(
        view: MockableMavericksView,
        viewModelProperty: KProperty<*>,
        existingViewModel: Boolean,
        stateClass: Class<S>,
        forceMockExistingViewModel: Boolean
    ): S? {

        val mockInfo = if (existingViewModel && forceMockExistingViewModel) {
            check(!stateMap.containsKey(view)) {
                "Expected to force mock existing view model, but mocked state already " +
                    "exists (${view.javaClass.simpleName}#${viewModelProperty.name})"
            }

            MavericksViewMocks.getFrom(view).mocks
                .firstOrNull { it.isDefaultState }
                ?.let { mvRxMock ->
                    @Suppress("UNCHECKED_CAST")
                    mvRxMock as MavericksMock<MockableMavericksView, *>
                }
                ?: error(
                    "No mock state found in ${view.javaClass.simpleName} for ViewModel ${viewModelProperty.name}. " +
                        "A mock state must be provided to support this existing ViewModel."
                )
        } else {
            stateMap[view] ?: return null
        }

        @Suppress("UNCHECKED_CAST") val state =
            mockInfo.stateForViewModelProperty(viewModelProperty, existingViewModel)

        if (state == null && existingViewModel) {
            error("An 'existingViewModel' must have its state provided in the mocks")
        }

        // TODO Higher order view model support in Mavericks
        // It's possible for this viewmodel to be injected with another view model in its constructor.
        // In that case, the other viewmodel needs to be initialized first, otherwise it will crash.
        // Fragments should use the 'dependencies' property of the viewmodel delegate function to specify viewmodels to initialize first,
        // however, in the existingViewModel case we don't use 'dependencies' because we expect it to already exist, which is true in production.
        // However, for mocking it won't exist, so we need to force existing view models to be created first.
        // We only do this from non existing view models to prevent a loop.
        if (!existingViewModel) {
            val otherViewModelsOnView =
                delegateInfoMap[view]?.filter { it.viewModelProperty != viewModelProperty }
                    ?: error("No delegates registered for ${view.javaClass.simpleName}")

            otherViewModelsOnView
                .filter { it.existingViewModel }
                .forEach { it.viewModelDelegate.value }
        }

        @Suppress("UNCHECKED_CAST")
        return when {
            state == null -> null
            state as? S != null -> state
            else -> error("Expected state of type ${stateClass.simpleName} but found ${state::class.java.simpleName}")
        }
    }

    fun <VM : MavericksViewModel<S>, S : MavericksState> addViewModelDelegate(
        view: MockableMavericksView,
        existingViewModel: Boolean,
        viewModelProperty: KProperty<*>,
        viewModelDelegate: lifecycleAwareLazy<VM>
    ) {
        delegateInfoMap
            .getOrPut(view) { mutableListOf() }
            .also { delegateInfoList ->
                require(delegateInfoList.none { it.viewModelProperty == viewModelProperty }) {
                    "Delegate already registered for ${viewModelProperty.name}"
                }
            }
            .add(ViewModelDelegateInfo(existingViewModel, viewModelProperty, viewModelDelegate))
    }

    data class ViewModelDelegateInfo<VM : MavericksViewModel<S>, S : MavericksState>(
        val existingViewModel: Boolean,
        val viewModelProperty: KProperty<*>,
        val viewModelDelegate: lifecycleAwareLazy<VM>
    )
}
