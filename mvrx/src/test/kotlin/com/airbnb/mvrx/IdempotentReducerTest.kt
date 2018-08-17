package com.airbnb.mvrx

import org.junit.Test
import java.util.concurrent.Semaphore

data class IdempotentReducerState(val count: Int = 0) : MvRxState
class IdempotentReducerTest : BaseTest() {

    @Test
    fun impureReducerShouldFail() {
        val semaphore = Semaphore(0)
        class ImpureViewModel(override val initialState: IdempotentReducerState) : TestMvRxViewModel<IdempotentReducerState>() {
            private var count = 0
            fun impureReducer() {
                setState {
                    val state = copy(count = ++this@ImpureViewModel.count)
                    state
                }
            }
        }
        val impureViewModel = ImpureViewModel(IdempotentReducerState())
        impureViewModel.stateStore.semaphoreForTesting = semaphore
        impureViewModel.impureReducer()
        semaphore.acquire()
        assert(impureViewModel.stateStore.throwableForTesting is IllegalArgumentException)
    }

    @Test
    fun pureReducerShouldNotFail() {
        val semaphore = Semaphore(0)
        class PureViewModel(override val initialState: IdempotentReducerState) : TestMvRxViewModel<IdempotentReducerState>() {
            fun pureReducer() {
                setState {
                    val state = copy(count = count + 1)
                    state
                }
            }
        }

        val pureViewModel = PureViewModel(IdempotentReducerState())
        pureViewModel.stateStore.semaphoreForTesting = semaphore
        pureViewModel.pureReducer()
        semaphore.acquire()
        assert(pureViewModel.stateStore.throwableForTesting == null)
    }
}