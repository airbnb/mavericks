package com.airbnb.mvrx.todomvrx.todoapp.core

import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.BuildConfig
import com.airbnb.mvrx.MvRxState

abstract class MvRxViewModel<S : MvRxState> : BaseMvRxViewModel<S>() {
    override val debugMode = BuildConfig.DEBUG
}