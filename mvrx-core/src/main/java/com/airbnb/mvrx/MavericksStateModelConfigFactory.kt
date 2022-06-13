package com.airbnb.mvrx

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

/**
 * Factory for providing the [MavericksStateModelConfig] for each new ViewModel that is created.
 *
 * An instance of this must be set on [Mavericks.viewModelConfigFactory].
 *
 * A custom subclass of this may be used to allow you to override [buildConfig], but this should
 * generally not be necessary.
 */
interface MavericksStateModelConfigFactory {
    /**
     * True if debug checks should be run. Should be false for production builds.
     * When true, certain validations are applied to the ViewModel. These can be slow and should
     * not be used in production! However, they do help to catch common issues so it is highly
     * recommended that you enable debug when applicable.
     */
    val debugMode: Boolean

    /**
     * Provide a default context for viewModelScope. It will be added after [SupervisorJob]
     * and [Dispatchers.Main.immediate].
     */
    val contextOverride: CoroutineContext

    /**
     * Provide an additional context that will be used in the [CoroutinesStateStore]. All withState/setState calls will be executed in this context.
     * By default these calls are executed with a shared thread pool dispatcher that is private to [CoroutinesStateStore]
     */
    val storeContextOverride: CoroutineContext

    /**
     * Provide a context that will be added to the coroutine scope when a subscription is registered (eg [MavericksView.onEach]).
     *
     * By default subscriptions use [MavericksView.subscriptionLifecycleOwner] and [LifecycleOwner.lifecycleScope] to
     * retrieve a coroutine scope to launch the subscription in.
     */
    val subscriptionCoroutineContextOverride: CoroutineContext

    @InternalMavericksApi
    fun <S : MavericksState> provideConfig(
        viewModel: MavericksStateModel<S>,
        initialState: S
    ): MavericksStateModelConfig<S>
}