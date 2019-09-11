package com.airbnb.mvrx

import com.airbnb.mvrx.mock.MockPrinterConfiguration
import com.airbnb.mvrx.mock.MockStateHolder
import com.airbnb.mvrx.mock.MvRxViewModelConfigProvider

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
    var viewModelConfigProvider = MvRxViewModelConfigProvider()

    /**
     * This global instance enables mock states to be forced onto ViewModels as they are created.
     * This enable easy testing.
     *
     * The instance is exposed publicly so references to mocked views can be cleared after a test
     * completes.
     */
    val mockStateHolder = MockStateHolder()

    /**
     * Configuration for how mock state is generated.
     */
    var mockPrinterConfiguration: MockPrinterConfiguration = MockPrinterConfiguration()
}