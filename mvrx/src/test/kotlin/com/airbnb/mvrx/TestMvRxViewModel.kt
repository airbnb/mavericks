package com.airbnb.mvrx

abstract class TestMvRxViewModel<S : MvRxState>(initialState: S) : BaseMavericksViewModel<S>(initialState, debugMode = true)
