package com.airbnb.mvrx

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * Provides configuration for a [MavericksRepositoryConfig].
 */
@ExperimentalMavericksApi
abstract class MavericksRepositoryConfig<S : Any>(
    /**
     * If true, extra validations will be applied to ensure the repository is used correctly.
     */
    val debugMode: Boolean,

    /**
     * The state store instance that will control the state of the repository.
     */
    val stateStore: MavericksStateStore<S>,

    /**
     * The coroutine scope that will be provided to the repository.
     */
    val coroutineScope: CoroutineScope,

    /**
     * Provide a context that will be added to the coroutine scope when a subscription is registered (eg [MavericksRepository.onEach]).
     *
     * By default subscriptions use [coroutineScope] to launch the subscription in.
     */
    val subscriptionCoroutineContextOverride: CoroutineContext
)
