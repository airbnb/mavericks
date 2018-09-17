package com.airbnb.mvrx

import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ScriptableStateStoreTest : BaseTest() {

    private lateinit var viewModel: TestViewModel

    @Before
    fun setup() {
        viewModel = TestViewModel()
    }

    @Test
    fun testSetStateCallsIgnored() {
        viewModel.attemptToChageState(2)
        withState(viewModel) {
            Assert.assertEquals(1, it.foo)
        }
    }

    @Test
    fun testCanScriptState() {
        viewModel.stateStore.next(TestState(foo = 2))
        withState(viewModel) {
            Assert.assertEquals(2, it.foo)
        }
    }

    data class TestState(val foo: Int = 1) : MvRxState

    private class TestViewModel(
        initialState: TestState = TestState(),
        val stateStore: ScriptableMvRxStateStore<TestState> = ScriptableMvRxStateStore(initialState)
    ) : BaseMvRxViewModel<TestState>(
        initialState,
        debugMode = true,
        stateStore = stateStore
    ) {

        fun attemptToChageState(newFoo: Int) {
            setState { copy(foo = newFoo) }
        }
    }

}
