package com.airbnb.mvrx

import org.junit.Test
import java.util.concurrent.Semaphore

data class IdempotentReducerState(val count: Int = 0) : MvRxState
class MvRxIdempotentReducerTest : MvRxBaseTest() {

    @Test(expected = IllegalArgumentException::class)
    fun impureReducerShouldFail() {
        val semaphore = Semaphore(0)
        class ImpureViewModel(override val initialState: IdempotentReducerState) : TestMvRxViewModel<IdempotentReducerState>() {
            private var count = 0
            fun impureReducer() {
                setState {
                    val state = copy(count = ++this@ImpureViewModel.count)
                    semaphore.release()
                    state
                }
            }
        }
        ImpureViewModel(IdempotentReducerState()).impureReducer()
        semaphore.acquire()
    }

    @Test
    fun pureReducerShouldNotFail() {
        val semaphore = Semaphore(0)
        class ImpureViewModel(override val initialState: IdempotentReducerState) : TestMvRxViewModel<IdempotentReducerState>() {

            fun pureReducer() {
                setState {
                    val state = copy(count = count + 1)
                    semaphore.release()
                    state
                }
            }
        }
        ImpureViewModel(IdempotentReducerState()).pureReducer()
        semaphore.acquire()
    }
}