package com.airbnb.mvrx

abstract class TestMvRxViewModel<S : MvRxState> : BaseMvRxViewModel<S>() {
    override val debugMode = true
}