package com.airbnb.mvrx


/**
 * This singleton can be used to provide mocked states to viewModels for testing.
 * State can be directly set or set via arguments. Set the mocker to enabled
 * to have the viewModels use these mocked states.
 */
object MvRxMocker {

    /**
     * Enable or disable whether the viewModels use mocked states
     */
    var enabled: Boolean = false
    private val mockedState: MutableMap<MvRxStateStore<*>, MvRxState?> = mutableMapOf()

    /**
     * Set a mock state on a viewModel directly
     */
    fun <S : MvRxState> setMockedState(viewModel: BaseMvRxViewModel<S>, state: S?) {
        mockedState[viewModel.stateStore] = state
    }

    /**
     * Set a mock state on a viewModel via arguments
     */
    fun <S : MvRxState> setMockedStateFromArgs(viewModel: BaseMvRxViewModel<S>, args: Any?) {
        setMockedState(viewModel, _initialStateProvider(viewModel.state::class.java, args))
    }

    /**
     * Return the mocked state for a viewModel if one has been set, otherwise return null
     */
    internal fun <S : Any> getMockedState(stateStore: MvRxStateStore<S>): S? {
        @Suppress("UNCHECKED_CAST")
        return if (enabled) mockedState[stateStore] as? S else null
    }
}
