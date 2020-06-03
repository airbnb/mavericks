package com.airbnb.mvrx

abstract class TestMavericksViewModel<S : MvRxState>(initialState: S) : MavericksViewModel<S>(initialState)
