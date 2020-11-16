package com.airbnb.mvrx

import kotlinx.coroutines.isActive
import org.junit.Assert.assertFalse
import org.junit.Test

class BaseViewModelTest : BaseTest() {
    data class TestState(val foo: Int = 5) : MavericksState
    class TestViewModel : MavericksViewModel<TestState>(TestState()) {
        // Make viewModelScope public
        val scope = viewModelScope
    }

    @Test
    fun testScopeIsCancelled() {
        val viewModel = TestViewModel()
        viewModel.onCleared()
        assertFalse(viewModel.scope.isActive)
    }
}