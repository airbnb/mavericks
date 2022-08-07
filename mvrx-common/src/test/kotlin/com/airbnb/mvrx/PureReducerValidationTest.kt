package com.airbnb.mvrx

import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

data class PureReducerValidationState(val count: Int = 0) : MavericksState
data class StateWithPrivateVal(private val count: Int = 0) : MavericksState

class PureReducerValidationTest : BaseTest() {

    @get:Rule
    @Suppress("DEPRECATION")
    var thrown = ExpectedException.none()!!

    @Test
    fun impureReducerShouldFail() {
        class ImpureRepository(initialState: PureReducerValidationState) : BaseTestMavericksRepository<PureReducerValidationState>(initialState) {
            private var count = 0
            fun impureReducer() {
                setState {
                    val state = copy(count = ++this@ImpureRepository.count)
                    state
                }
            }
        }
        thrown.expect(IllegalArgumentException::class.java)
        thrown.expectMessage("Impure reducer set on ImpureRepository! count changed from 1 to 2. Ensure that your state properties properly implement hashCode.")
        ImpureRepository(PureReducerValidationState()).impureReducer()
    }

    @Test
    fun pureReducerShouldNotFail() {
        class PureRepository(initialState: PureReducerValidationState) : BaseTestMavericksRepository<PureReducerValidationState>(initialState) {
            fun pureReducer() {
                setState {
                    val state = copy(count = count + 1)
                    state
                }
            }
        }
        PureRepository(PureReducerValidationState()).pureReducer()
    }

    @Test
    fun shouldBeAbleToUsePrivateProps() {
        class PureRepository(initialState: StateWithPrivateVal) : BaseTestMavericksRepository<StateWithPrivateVal>(initialState) {
            fun pureReducer() {
                setState { this }
            }
        }
        PureRepository(StateWithPrivateVal()).pureReducer()
    }

    @Test
    fun impureReducerWithPrivatePropShouldFail() {
        class ImpureRepository(initialState: StateWithPrivateVal) : BaseTestMavericksRepository<StateWithPrivateVal>(initialState) {
            private var count = 0
            fun impureReducer() {
                setState {
                    val state = copy(count = ++this@ImpureRepository.count)
                    state
                }
            }
        }

        thrown.expect(IllegalArgumentException::class.java)
        thrown.expectMessage("Impure reducer set on ImpureRepository! count changed from 1 to 2. Ensure that your state properties properly implement hashCode.")
        ImpureRepository(StateWithPrivateVal()).impureReducer()
    }
}
