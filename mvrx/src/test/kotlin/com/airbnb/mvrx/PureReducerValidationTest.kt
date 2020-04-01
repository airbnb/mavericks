package com.airbnb.mvrx

import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

data class PureReducerValidationState(val count: Int = 0) : MvRxState
data class StateWithPrivateVal(private val count: Int = 0) : MvRxState

class PureReducerValidationTest : BaseTest() {

    @get:Rule
    var thrown = ExpectedException.none()!!

    @Test
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
        thrown.expect(IllegalArgumentException::class.java)
        thrown.expectMessage("Impure reducer set on impureReducerShouldFail\$ImpureViewModel! count changed from 1 to 2. Ensure that your state properties properly implement hashCode.")
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

    @Test
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

        thrown.expect(IllegalArgumentException::class.java)
        thrown.expectMessage("Impure reducer set on impureReducerWithPrivatePropShouldFail\$ImpureViewModel! count changed from 1 to 2. Ensure that your state properties properly implement hashCode.")
        ImpureViewModel(StateWithPrivateVal()).impureReducer()
    }
}
