package com.airbnb.mvrx

import kotlinx.coroutines.isActive
import org.junit.Assert.assertFalse
import org.junit.Test

class BaseViewModelTest : BaseTest() {
    data class TestState(val foo: Int = 5) : MvRxState
    class TestViewModel : BaseMavericksViewModel<TestState>(TestState(), debugMode = false) {
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