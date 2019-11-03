package com.airbnb.mvrx

object MvRx {
    /**
     * If your initial state needs to use Fragment arguments, store your arguments
     * as a parcelable class stored at this key.
     */
    const val KEY_ARG = "mvrx:arg"

    /**
     * A factory for creating a [MvRxViewModelConfig] for each ViewModel.
     *
     * You MUST provide an instance here before creating any viewmodels. You can do this when
     * your application is created.
     *
     * This allows you to specify whether MvRx should run in debug mode or not. Additionally, it
     * allows custom state stores or execution behavior for the ViewModel, which can be helpful
     * for testing.
     */
    var viewModelConfigFactory: MvRxViewModelConfigFactory? = null

    var viewModelDelegateFactory: ViewModelDelegateFactory =
        DefaultGlobalViewModelFactory()

    internal val nonNullViewModelConfigFactory: MvRxViewModelConfigFactory
        get() {
            return viewModelConfigFactory
                ?: error("You must specify a viewModelConfigProvider in the MvRx object")
        }
}