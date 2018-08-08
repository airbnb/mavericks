package com.airbnb.mvrx.sample.core

import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.sample.BuildConfig

abstract class MvRxViewModel<S : MvRxState> : BaseMvRxViewModel<S>() {
    override val debugMode = BuildConfig.DEBUG
}