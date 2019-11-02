package com.airbnb.mvrx

/**
 * Switch between using a mock view model store and a normal view model store.
 *
 * @param debugMode True if this is a debug build of the app, false for production builds.
 * When true,
 */
open class MvRxViewModelConfigProvider(val debugMode: Boolean) {

    private val onConfigProvidedListener =
        mutableListOf<(BaseMvRxViewModel<*>, MvRxViewModelConfig<*>) -> Unit>()


    fun <S : MvRxState> provideConfig(
        viewModel: BaseMvRxViewModel<S>,
        initialState: S
    ): MvRxViewModelConfig<S> {
        return buildConfig(initialState).also { config ->
            onConfigProvidedListener.forEach { callback -> callback(viewModel, config) }
        }
    }

    open fun <S : MvRxState> buildConfig(initialState: S): MvRxViewModelConfig<S> {
        return object : MvRxViewModelConfig<S>(debugMode,
            RealMvRxStateStore(initialState)
        ) {
            override fun <S : MvRxState> onExecute(viewModel: BaseMvRxViewModel<S>): BlockExecutions {
                return BlockExecutions.No
            }
        }
    }


    /**
     * Add a listener that will be called every time a [MvRxViewModelConfig] is created for a new
     * view model. This will happen each time a new ViewModel is created.
     *
     * The callback includes a reference to the ViewModel that the config was created for, as well
     * as the configuration itself.
     */
    fun addOnConfigProvidedListener(callback: (BaseMvRxViewModel<*>, MvRxViewModelConfig<*>) -> Unit) {
        onConfigProvidedListener.add(callback)
    }

    fun removeOnConfigProvidedListener(callback: (BaseMvRxViewModel<*>, MvRxViewModelConfig<*>) -> Unit) {
        onConfigProvidedListener.remove(callback)
    }
}