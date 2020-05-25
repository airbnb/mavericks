package com.airbnb.mvrx

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.annotation.RestrictTo
import kotlinx.coroutines.GlobalScope

/**
 * Factory for providing the [MavericksViewModelConfig] for each new ViewModel that is created.
 *
 * An instance of this must be set on [MvRx.viewModelConfigFactory].
 *
 * A custom subclass of this may be used to allow you to override [buildConfig], but this should
 * generally not be necessary.
 *
 * @param debugMode True if debug checks should be run. Should be false for production builds.
 * When true, certain validations are applied to the ViewModel. These can be slow and should
 * not be used in production! However, they do help to catch common issues so it is highly
 * recommended that you enable debug when applicable.
 */
open class MavericksViewModelConfigFactory(val debugMode: Boolean) {

    /**
     * Sets [debugMode] depending on whether the app was built with the Debuggable flag enabled.
     */
    constructor(context: Context) : this(context.isDebuggable())

    private val onConfigProvidedListener =
            mutableListOf<(BaseMavericksViewModel<*>, MavericksViewModelConfig<*>) -> Unit>()


    internal fun <S : MvRxState> provideConfig(
            viewModel: BaseMavericksViewModel<S>,
            initialState: S
    ): MavericksViewModelConfig<S> {
        return buildConfig(viewModel, initialState).also { config ->
            onConfigProvidedListener.forEach { callback -> callback(viewModel, config) }
        }
    }

    /**
     * Create a new [MavericksViewModelConfig] for the given viewmodel.
     * This can be overridden to customize the config.
     */
    open fun <S : MvRxState> buildConfig(
            viewModel: BaseMavericksViewModel<S>,
            initialState: S
    ): MavericksViewModelConfig<S> {
        // TODO: Pass scope
        return object : MavericksViewModelConfig<S>(debugMode, CoroutinesStateStore(initialState, GlobalScope)) {
            override fun <S : MvRxState> onExecute(viewModel: BaseMavericksViewModel<S>): BlockExecutions {
                return BlockExecutions.No
            }
        }
    }


    /**
     * Add a listener that will be called every time a [MavericksViewModelConfig] is created for a new
     * view model. This will happen each time a new ViewModel is created.
     *
     * The callback includes a reference to the ViewModel that the config was created for, as well
     * as the configuration itself.
     */
    fun addOnConfigProvidedListener(callback: (BaseMavericksViewModel<*>, MavericksViewModelConfig<*>) -> Unit) {
        onConfigProvidedListener.add(callback)
    }

    fun removeOnConfigProvidedListener(callback: (BaseMavericksViewModel<*>, MavericksViewModelConfig<*>) -> Unit) {
        onConfigProvidedListener.remove(callback)
    }
}


@RestrictTo(RestrictTo.Scope.LIBRARY)
fun Context.isDebuggable(): Boolean = (0 != (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE))