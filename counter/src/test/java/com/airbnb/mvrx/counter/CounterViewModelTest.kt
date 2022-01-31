package com.airbnb.mvrx.counter

import com.airbnb.mvrx.test.MvRxTestExtension
import com.airbnb.mvrx.withState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class CounterViewModelTest {

    @Test
    fun testIncrementCount() {
        val viewModel = CounterViewModel(CounterState())
        viewModel.incrementCount()
        withState(viewModel) { state ->
            assertEquals(1, state.count)
        }
    }

    companion object {
        @JvmField
        @RegisterExtension
        val mvrxTestExtension = MvRxTestExtension()
    }
}
