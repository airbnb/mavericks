package com.airbnb.mvrx

object MvRx {
    /**
     * If your initial state needs to use Fragment arguments, store your arguments
     * as a parcelable class stored at this key.
     */
    const val KEY_ARG = "mvrx:arg"

    /**
     * Defines configuration for how ViewModels are created and what settings they use.
     * By default this applies debug settings to all ViewModels and SHOULD be overridden in
     * production builds to disable debug mode for performance reasons.
     *
     * Additionally, this allows various mock settings to be applied to ViewModels to enable
     * forcing states for testing and development.
     */
    var viewModelConfigProvider: MvRxViewModelConfigProvider? = null

    var viewModelProviderFactory: ViewModelProviderFactory =
        DefaultViewModelProviderFactory()

    internal val nonNullViewModelConfigProvider: MvRxViewModelConfigProvider
        get() {
            return viewModelConfigProvider
                ?: error("You must specify a viewModelConfigProvider in the MvRx object")
        }
}