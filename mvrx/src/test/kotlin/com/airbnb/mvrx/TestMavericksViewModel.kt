package com.airbnb.mvrx

abstract class TestMavericksViewModel<S : MavericksState>(initialState: S) : MavericksViewModel<S>(initialState)
