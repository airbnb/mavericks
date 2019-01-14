package com.airbnb.mvrx

import org.junit.Test

data class PureReducerValidationState(val count: Int = 0) : MvRxState
data class StateWithPrivateVal(private val count: Int = 0) : MvRxState

class PureReducerValidationTest : BaseTest() {

    @Test(expected = IllegalArgumentException::class)
    fun impureReducerShouldFail() {
        class ImpureViewModel(initialState: PureReducerValidationState) : TestMvRxViewModel<PureReducerValidationState>(initialState) {
            private var count = 0
            fun impureReducer() {
                setState {
                    val state = copy(count = ++this@ImpureViewModel.count)
                    state
                }
            }
        }
        ImpureViewModel(PureReducerValidationState()).impureReducer()
    }

    @Test
    fun pureReducerShouldNotFail() {
        class PureViewModel(initialState: PureReducerValidationState) : TestMvRxViewModel<PureReducerValidationState>(initialState) {
            fun pureReducer() {
                setState {
                    val state = copy(count = count + 1)
                    state
                }
            }
        }
        PureViewModel(PureReducerValidationState()).pureReducer()
    }

    @Test
    fun shouldBeAbleToUsePrivateProps() {
        class PureViewModel(initialState: StateWithPrivateVal) : TestMvRxViewModel<StateWithPrivateVal>(initialState) {
            fun pureReducer() {
                setState { this }
            }
        }
        PureViewModel(StateWithPrivateVal()).pureReducer()
    }

    @Test(expected = IllegalArgumentException::class)
    fun impureReducerWithPrivatePropShouldFail() {
        class ImpureViewModel(initialState: StateWithPrivateVal) : TestMvRxViewModel<StateWithPrivateVal>(initialState) {
            private var count = 0
            fun impureReducer() {
                setState {
                    val state = copy(count = ++this@ImpureViewModel.count)
                    state
                }
            }
        }
        ImpureViewModel(StateWithPrivateVal()).impureReducer()
    }
}