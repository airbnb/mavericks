package com.airbnb.mvrx.mocking

import com.airbnb.mvrx.BaseMavericksViewModel
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.withState
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ScriptableStateStoreTest : BaseTest() {

    private lateinit var viewModel: TestViewModel

    @Before
    fun setup() {
        MavericksMocks.mockConfigFactory.withMockBehavior(
            MockBehavior(stateStoreBehavior = MockBehavior.StateStoreBehavior.Scriptable)
        ) {
            viewModel = TestViewModel()
        }
    }

    @Test
    fun testSetStateCallsIgnored() {
        viewModel.attemptToChangeState(2)
        withState(viewModel) {
            Assert.assertEquals(1, it.foo)
        }
    }

    @Test
    fun testCanScriptState() {
        MavericksMocks.setScriptableState(viewModel, TestState(foo = 2))
        withState(viewModel) {
            Assert.assertEquals(2, it.foo)
        }
    }

    data class TestState(val foo: Int = 1) : MvRxState

    private class TestViewModel(
        initialState: TestState = TestState()
    ) : BaseMavericksViewModel<TestState>(initialState) {

        fun attemptToChangeState(newFoo: Int) {
            setState { copy(foo = newFoo) }
        }
    }
}
