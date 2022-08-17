package com.airbnb.mvrx.todomvrx.core

import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.MavericksState

abstract class MvRxViewModel<S : MavericksState>(initialState: S) : BaseMvRxViewModel<S>(initialState)
