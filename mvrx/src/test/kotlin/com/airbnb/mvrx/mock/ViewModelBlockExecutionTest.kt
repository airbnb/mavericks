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
        val configFactory = MockMvRxViewModelConfigFactory(null)

        configFactory.mockBehavior = MockBehavior(
            MockBehavior.InitialStateMocking.None,
            MvRxViewModelConfig.BlockExecutions.No,
            MockBehavior.StateStoreBehavior.Synchronous
        )

        val vm = ViewModel(configFactory = configFactory)
        vm.setNumAsync(1)
        assertEquals(1, vm.state.num())
    }

    @Test
    fun executeBlockedCompletely() {
        val configFactory = MockMvRxViewModelConfigFactory(null)

        configFactory.mockBehavior = MockBehavior(
            MockBehavior.InitialStateMocking.None,
            MvRxViewModelConfig.BlockExecutions.Completely,
            MockBehavior.StateStoreBehavior.Synchronous
        )

        val vm = ViewModel(configFactory = configFactory)
        vm.setNumAsync(1)
        assertEquals(Uninitialized, vm.state.num)
    }

    @Test
    fun executeBlockedWithLoading() {
        val configFactory = MockMvRxViewModelConfigFactory(null)

        configFactory.mockBehavior = MockBehavior(
            MockBehavior.InitialStateMocking.None,
            MvRxViewModelConfig.BlockExecutions.WithLoading,
            MockBehavior.StateStoreBehavior.Synchronous
        )

        val vm = ViewModel(configFactory = configFactory)
        vm.setNumAsync(1)
        assertEquals(Loading<Int>(), vm.state.num)
    }

    data class State(val num: Async<Int> = Uninitialized) : MvRxState
    class ViewModel(
        state: State = State(),
        configFactory: MockMvRxViewModelConfigFactory = MockMvRxViewModelConfigFactory(
            null
        )
    ) : BaseMvRxViewModel<State>(state, configFactory) {
        fun setNumAsync(value: Int) {
            Observable.just(value)
                .execute { copy(num = it) }
        }
    }
}