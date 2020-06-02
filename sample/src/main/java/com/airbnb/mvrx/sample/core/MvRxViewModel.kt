package com.airbnb.mvrx.sample.core

import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MvRxState

abstract class MvRxViewModel<S : MvRxState>(initialState: S) : MavericksViewModel<S>(initialState)
