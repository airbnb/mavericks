package com.airbnb.mvrx.todomvrx.core

import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.BuildConfig
import com.airbnb.mvrx.MvRxState

abstract class MvRxViewModel<S : MvRxState> : BaseMvRxViewModel<S>() {
    override val debugMode = BuildConfig.DEBUG
}