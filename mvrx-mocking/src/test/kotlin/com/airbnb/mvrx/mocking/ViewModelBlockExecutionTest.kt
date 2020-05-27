package com.airbnb.mvrx.mocking

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.BaseMavericksViewModel
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksViewModelConfig
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.Uninitialized
import org.junit.Assert.assertEquals
import org.junit.Test

class ViewModelBlockExecutionTest : BaseTest() {
    @Test
    fun executeNormally() {
        MavericksMocks.mockConfigFactory.mockBehavior = MockBehavior(
            stateStoreBehavior = MockBehavior.StateStoreBehavior.Synchronous
        )

        val vm = ViewModel()
        vm.setNumAsync(1)
        assertEquals(1, vm.state().num())
    }

    @Test
    fun executeBlockedCompletely() {
        MavericksMocks.mockConfigFactory.mockBehavior = MockBehavior(
            blockExecutions = MavericksViewModelConfig.BlockExecutions.Completely,
            stateStoreBehavior = MockBehavior.StateStoreBehavior.Synchronous
        )

        val vm = ViewModel()
        vm.setNumAsync(1)
        assertEquals(Uninitialized, vm.state().num)
    }

    @Test
    fun executeBlockedWithLoading() {
        MavericksMocks.mockConfigFactory.mockBehavior = MockBehavior(
            blockExecutions = MavericksViewModelConfig.BlockExecutions.WithLoading,
            stateStoreBehavior = MockBehavior.StateStoreBehavior.Synchronous
        )

        val vm = ViewModel()
        vm.setNumAsync(1)
        assertEquals(Loading<Int>(), vm.state().num)
    }

    data class State(val num: Async<Int> = Uninitialized) : MvRxState
    class ViewModel(
        state: State = State()
    ) : BaseMavericksViewModel<State>(state) {
        fun setNumAsync(value: Int) {
            // TODO: Test rxjava too
            suspend { value }.execute {
                copy(num = it)
            }
        }
    }
}

fun <S : MvRxState, VM : BaseMavericksViewModel<S>> VM.state(): S = config.stateStore.state