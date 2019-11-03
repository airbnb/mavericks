package com.airbnb.mvrx

/**
 *
 */
abstract class MvRxViewModelConfig<S : Any>(
    val debugMode: Boolean,
    @PublishedApi internal val stateStore: MvRxStateStore<S>
) {
    abstract fun <S : MvRxState> onExecute(
        viewModel: BaseMvRxViewModel<S>
    ): BlockExecutions

    enum class BlockExecutions {
        No,
        Completely,
        WithLoading
    }
}