package com.airbnb.mvrx

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Factory for providing the [MavericksStateModelConfig] for each new ViewModel that is created.
 *
 * An instance of this must be set on [Mavericks.viewModelConfigFactory].
 *
 * A custom subclass of this may be used to allow you to override [buildConfig], but this should
 * generally not be necessary.
 */
open class MavericksViewModelConfigFactory(
    /**
     * True if debug checks should be run. Should be false for production builds.
     * When true, certain validations are applied to the ViewModel. These can be slow and should
     * not be used in production! However, they do help to catch common issues so it is highly
     * recommended that you enable debug when applicable.
     */
    override val debugMode: Boolean,
    /**
     * Provide a default context for viewModelScope. It will be added after [SupervisorJob]
     * and [Dispatchers.Main.immediate].
     */
    override val contextOverride: CoroutineContext = EmptyCoroutineContext,
    /**
     * Provide an additional context that will be used in the [CoroutinesStateStore]. All withState/setState calls will be executed in this context.
     * By default these calls are executed with a shared thread pool dispatcher that is private to [CoroutinesStateStore]
     */
    override val storeContextOverride: CoroutineContext = EmptyCoroutineContext,
    /**
     * Provide a context that will be added to the coroutine scope when a subscription is registered (eg [MavericksView.onEach]).
     *
     * By default subscriptions use [MavericksView.subscriptionLifecycleOwner] and [LifecycleOwner.lifecycleScope] to
     * retrieve a coroutine scope to launch the subscription in.
     */
    override val subscriptionCoroutineContextOverride: CoroutineContext = EmptyCoroutineContext,
) : MavericksStateModelConfigFactory {

    /**
     * Sets [debugMode] depending on whether the app was built with the Debuggable flag enabled.
     */
    constructor(
        context: Context,
        contextOverride: CoroutineContext = EmptyCoroutineContext,
        storeContextOverride: CoroutineContext = EmptyCoroutineContext
    ) : this(context.isDebuggable(), contextOverride, storeContextOverride)

    private val onConfigProvidedListener =
        mutableListOf<(MavericksStateModel<*>, MavericksStateModelConfig<*>) -> Unit>()

    open fun coroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate + contextOverride)
    }

    override fun <S : MavericksState> provideConfig(
        viewModel: MavericksStateModel<S>,
        initialState: S
    ): MavericksStateModelConfig<S> {
        return buildConfig(viewModel, initialState).also { config ->
            onConfigProvidedListener.forEach { callback -> callback(viewModel, config) }
        }
    }

    /**
     * Create a new [MavericksStateModelConfig] for the given viewmodel.
     * This can be overridden to customize the config.
     */
    open fun <S : MavericksState> buildConfig(
        viewModel: MavericksStateModel<S>,
        initialState: S
    ): MavericksStateModelConfig<S> {
        val scope = coroutineScope()
        return object : MavericksStateModelConfig<S>(debugMode, CoroutinesStateStore(initialState, scope, storeContextOverride), scope) {
            override fun <S : MavericksState> onExecute(viewModel: MavericksStateModel<S>): BlockExecutions {
                return BlockExecutions.No
            }

            override val subscriptionCoroutineContextOverride: CoroutineContext = this@MavericksViewModelConfigFactory.subscriptionCoroutineContextOverride

            override val verifyStateImmutability: Boolean = this@MavericksViewModelConfigFactory.debugMode
        }
    }

    /**
     * Add a listener that will be called every time a [MavericksStateModelConfig] is created for a new
     * view model. This will happen each time a new ViewModel is created.
     *
     * The callback includes a reference to the ViewModel that the config was created for, as well
     * as the configuration itself.
     */
    fun addOnConfigProvidedListener(callback: (MavericksStateModel<*>, MavericksStateModelConfig<*>) -> Unit) {
        onConfigProvidedListener.add(callback)
    }

    fun removeOnConfigProvidedListener(callback: (MavericksStateModel<*>, MavericksStateModelConfig<*>) -> Unit) {
        onConfigProvidedListener.remove(callback)
    }
}

internal fun Context.isDebuggable(): Boolean = (0 != (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE))
