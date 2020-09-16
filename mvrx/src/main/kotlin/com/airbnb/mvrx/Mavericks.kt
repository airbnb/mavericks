package com.airbnb.mvrx

import android.content.Context

object Mavericks {
    /**
     * If your initial state needs to use Fragment arguments, store your arguments
     * as a parcelable class stored at this key.
     */
    const val KEY_ARG = "mavericks:arg"

    /**
     * A factory that provides the Lazy ViewModels created for Mavericks extension functions,
     * such as [activityViewModel].
     *
     * Each time a ViewModel is accessed via one of these extension functions this factory
     * creates the Kotlin property delegate that wraps the ViewModel.
     *
     * The default implementation [DefaultViewModelDelegateFactory] is fine for general usage,
     * but a custom factory may be provided to assist with testing, or if you want control
     * over how and when ViewModels and their State are created.
     */
    var viewModelDelegateFactory: ViewModelDelegateFactory = DefaultViewModelDelegateFactory()

    /**
     * A factory for creating a [MavericksViewModelConfig] for each ViewModel.
     *
     * You MUST provide an instance here before creating any viewmodels. You can do this when
     * your application is created via the [initialize] helper.
     *
     * This allows you to specify whether Mavericks should run in debug mode or not. Additionally, it
     * allows custom state stores or execution behavior for the ViewModel, which can be helpful
     * for testing.
     */
    var viewModelConfigFactory: MavericksViewModelConfigFactory
        set(value) {
            _viewModelConfigFactory = value
        }
        get() {
            return _viewModelConfigFactory ?: error("You must initialize Mavericks. Add Mavericks.initialize(...) to your Application.onCreate().")
        }
    private var _viewModelConfigFactory: MavericksViewModelConfigFactory? = null

    /**
     * A helper for setting [viewModelConfigFactory] based on whether the app was built in debug mode or not.
     */
    fun initialize(
        context: Context,
        viewModelConfigFactory: MavericksViewModelConfigFactory? = null,
        viewModelDelegateFactory: ViewModelDelegateFactory? = null
    ) {
        initialize(context.isDebuggable(), viewModelConfigFactory, viewModelDelegateFactory)
    }

    /**
     * A helper for setting [viewModelConfigFactory] with the given debug mode.
     */
    fun initialize(
        debugMode: Boolean,
        viewModelConfigFactory: MavericksViewModelConfigFactory? = null,
        viewModelDelegateFactory: ViewModelDelegateFactory? = null
    ) {
        _viewModelConfigFactory = viewModelConfigFactory ?: MavericksViewModelConfigFactory(debugMode = debugMode)
        this.viewModelDelegateFactory = when {
            viewModelDelegateFactory != null -> viewModelDelegateFactory
            this.viewModelDelegateFactory !is DefaultViewModelDelegateFactory -> DefaultViewModelDelegateFactory()
            else -> this.viewModelDelegateFactory
        }
    }
}
