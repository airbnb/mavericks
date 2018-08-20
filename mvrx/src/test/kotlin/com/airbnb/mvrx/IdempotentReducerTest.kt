package com.airbnb.mvrx

import org.junit.Test
import java.util.concurrent.Semaphore

data class IdempotentReducerState(val count: Int = 0) : MvRxState
class IdempotentReducerTest : BaseTest() {

    @Test(expected = IllegalArgumentException::class)
    fun impureReducerShouldFail() {
        class ImpureViewModel(override val initialState: IdempotentReducerState) : TestMvRxViewModel<IdempotentReducerState>() {
            private var count = 0
            fun impureReducer() {
                setState {
                    val state = copy(count = ++this@ImpureViewModel.count)
                    state
                }
            }
        }
        ImpureViewModel(IdempotentReducerState()).impureReducer()
    }

    @Test
    fun pureReducerShouldNotFail() {
        class PureViewModel(override val initialState: IdempotentReducerState) : TestMvRxViewModel<IdempotentReducerState>() {
            fun pureReducer() {
                setState {
                    val state = copy(count = count + 1)
                    state
                }
            }
        }
        PureViewModel(IdempotentReducerState()).pureReducer()
    }
}