package com.airbnb.mvrx

import kotlinx.coroutines.CoroutineScope

/**
 * Provides configuration for a [MavericksViewModel].
 */
abstract class MavericksViewModelConfig<S : Any>(
    /**
     * If true, extra validations will be applied to ensure the view model is used
     * correctly.
     */
    val debugMode: Boolean,
    /**
     * The state store instance that will control the state of the ViewModel.
     */
    @InternalMavericksApi
    val stateStore: MavericksStateStore<S>,
    /**
     * The coroutine scope that will be provided to the view model.
     */
    val coroutineScope: CoroutineScope
) {
    /**
     * Called each time a [MavericksViewModel.execute] function is invoked. This allows
     * the execute function to be skipped, based on the returned [BlockExecutions] value.
     *
     * This is intended to be used to allow the ViewModel to be mocked out for testing.
     * Blocking calls to execute prevents long running asynchronous operations from changing the
     * state later on when the calls complete.
     *
     * Mocking out the state store cannot accomplish this on its own, because in some cases we may
     * want the state store to initially be mocked, with state changes blocked, but later on we may
     * want it to allow state changes.
     *
     * This prevents the case of an executed async call from modifying state once the state stored
     * is "enabled", even if the execute was performed when the state store was "disabled" and we
     * didn't intend to allow operations to change the state.
     */
    abstract fun <S : MavericksState> onExecute(
        viewModel: MavericksViewModel<S>
    ): BlockExecutions

    /**
     * Defines whether a [MavericksViewModel.execute] invocation should not be run.
     */
    enum class BlockExecutions {
        /** Run the execute block normally. */
        No,

        /** Block the execute call from having an impact. */
        Completely,

        /**
         * Block the execute call from having an impact from values returned by the object
         * being executed, but perform one state callback to set the Async property to loading
         * as if the call is actually happening.
         */
        WithLoading
    }
}
