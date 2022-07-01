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
    debugMode: Boolean,
    /**
     * The state store instance that will control the state of the ViewModel.
     */
    stateStore: MavericksStateStore<S>,
    /**
     * The coroutine scope that will be provided to the view model.
     */
    coroutineScope: CoroutineScope,
    /**
     * Provide a context that will be added to the coroutine scope when a subscription is registered (eg [MavericksView.onEach]).
     *
     * By default subscriptions use [MavericksView.subscriptionLifecycleOwner] and [LifecycleOwner.lifecycleScope] to
     * retrieve a coroutine scope to launch the subscription in.
     */
    subscriptionCoroutineContextOverride: CoroutineContext
) : MavericksRepositoryConfig<S>(
    debugMode = debugMode,
    stateStore = stateStore,
    coroutineScope = coroutineScope,
    subscriptionCoroutineContextOverride = subscriptionCoroutineContextOverride
)
