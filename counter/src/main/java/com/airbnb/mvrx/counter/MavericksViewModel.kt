package com.airbnb.mvrx.counter

import com.airbnb.mvrx.BaseMavericksViewModel
import com.airbnb.mvrx.MvRxState

abstract class MavericksViewModel<S : MvRxState>(state: S) : BaseMavericksViewModel<S>(state, debugMode = BuildConfig.DEBUG)