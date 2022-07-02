package com.airbnb.mvrx

import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

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
    val stateStore: MavericksStateStore<S>,
    /**
     * The coroutine scope that will be provided to the view model.
     */
    val coroutineScope: CoroutineScope,
    /**
     * Provide a context that will be added to the coroutine scope when a subscription is registered (eg [MavericksView.onEach]).
     *
     * By default subscriptions use [MavericksView.subscriptionLifecycleOwner] and [LifecycleOwner.lifecycleScope] to
     * retrieve a coroutine scope to launch the subscription in.
     */
    val subscriptionCoroutineContextOverride: CoroutineContext
) {
    /**
     * Called each time a [MavericksRepository.execute] function is invoked. This allows
     * the execute function to be skipped, based on the returned [MavericksBlockExecutions] value.
     *
     * This is intended to be used to allow the [MavericksRepository] to be mocked out for testing.
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
    abstract fun <S : MavericksState> onExecute(viewModel: MavericksViewModel<S>): MavericksBlockExecutions
}
