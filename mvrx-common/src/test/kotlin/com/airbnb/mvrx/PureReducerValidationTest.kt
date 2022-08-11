package com.airbnb.mvrx

import org.junit.Assert
import org.junit.Test

data class PureReducerValidationState(val count: Int = 0) : MavericksState
data class StateWithPrivateVal(private val count: Int = 0) : MavericksState

class PureReducerValidationTest : BaseTest() {

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
        Assert.assertThrows(
            "Impure reducer set on ImpureRepository! count changed from 1 to 2. Ensure that your state properties properly implement hashCode.",
            IllegalArgumentException::class.java
        ) {
            ImpureRepository(PureReducerValidationState()).impureReducer()
        }
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

        Assert.assertThrows(
            "Impure reducer set on ImpureRepository! count changed from 1 to 2. Ensure that your state properties properly implement hashCode.",
            IllegalArgumentException::class.java
        ) {
            ImpureRepository(StateWithPrivateVal()).impureReducer()
        }
    }
}
