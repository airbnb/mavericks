package com.airbnb.mvrx


object MvRxMocker {

    var enabled: Boolean = false
    private val mockedState: MutableMap<MvRxStateStore<*>, MvRxState> = mutableMapOf()

    fun <S : MvRxState> setMockedState(viewModel: BaseMvRxViewModel<S>, state: S) {
        mockedState[viewModel.stateStore] = state
    }

    fun <S : MvRxState> setMockedStateFromArgs(viewModel: BaseMvRxViewModel<S>, args: Any?) {
        mockedState[viewModel.stateStore] = _initialStateProvider(viewModel.state::class.java, args)
    }

    internal fun <S : Any> getMockedState(stateStore: MvRxStateStore<S>): S? {
        @Suppress("UNCHECKED_CAST")
        return if (enabled) mockedState[stateStore] as? S else null
    }
}
