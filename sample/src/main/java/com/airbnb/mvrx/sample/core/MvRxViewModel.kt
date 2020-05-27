package com.airbnb.mvrx.sample.core

import com.airbnb.mvrx.BaseMavericksViewModel
import com.airbnb.mvrx.MvRxState

abstract class MvRxViewModel<S : MvRxState>(initialState: S) : BaseMavericksViewModel<S>(initialState)
