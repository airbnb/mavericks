package com.airbnb.mvrx

import android.os.Parcelable


object MvRxMocker {

    var enabled: Boolean = false
    private val mockedState: MutableMap<BaseMvRxViewModel<*>, Any> = mutableMapOf()

     fun <S : MvRxState> setMockedState(viewModel: BaseMvRxViewModel<S>, state: S) {
        mockedState[viewModel] = state
    }

     fun <S : MvRxState> setMockedStateFromArg(viewModel: BaseMvRxViewModel<S>, args: Parcelable) {
        mockedState[viewModel] = TODO()
    }


    internal fun <S : MvRxState> getMockedState(viewModel: BaseMvRxViewModel<S>): S? {
        return if (enabled) mockedState[viewModel] as S else null
    }
}
