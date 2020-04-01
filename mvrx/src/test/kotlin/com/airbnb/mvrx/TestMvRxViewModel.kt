package com.airbnb.mvrx

abstract class TestMvRxViewModel<S : MvRxState>(initialState: S) : BaseMvRxViewModel<S>(initialState, debugMode = true)
