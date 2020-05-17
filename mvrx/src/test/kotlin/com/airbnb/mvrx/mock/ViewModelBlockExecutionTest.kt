package com.airbnb.mvrx.mock

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.BaseTest
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.MvRxViewModelConfig
import com.airbnb.mvrx.Uninitialized
import io.reactivex.Observable
import org.junit.Assert.assertEquals
import org.junit.Test

class ViewModelBlockExecutionTest : BaseTest() {
    @Test
    fun executeNormally() {
        MvRxMocks.mockConfigFactory.mockBehavior = MockBehavior(
            stateStoreBehavior = MockBehavior.StateStoreBehavior.Synchronous
        )

        val vm = ViewModel()
        vm.setNumAsync(1)
        assertEquals(1, vm.state.num())
    }

    @Test
    fun executeBlockedCompletely() {
        MvRxMocks.mockConfigFactory.mockBehavior = MockBehavior(
            blockExecutions = MvRxViewModelConfig.BlockExecutions.Completely,
            stateStoreBehavior = MockBehavior.StateStoreBehavior.Synchronous
        )

        val vm = ViewModel()
        vm.setNumAsync(1)
        assertEquals(Uninitialized, vm.state.num)
    }

    @Test
    fun executeBlockedWithLoading() {
        MvRxMocks.mockConfigFactory.mockBehavior = MockBehavior(
            blockExecutions = MvRxViewModelConfig.BlockExecutions.WithLoading,
            stateStoreBehavior = MockBehavior.StateStoreBehavior.Synchronous
        )

        val vm = ViewModel()
        vm.setNumAsync(1)
        assertEquals(Loading<Int>(), vm.state.num)
    }

    data class State(val num: Async<Int> = Uninitialized) : MvRxState
    class ViewModel(
        state: State = State()
    ) : BaseMvRxViewModel<State>(state) {
        fun setNumAsync(value: Int) {
            Observable.just(value)
                .execute { copy(num = it) }
        }
    }
}