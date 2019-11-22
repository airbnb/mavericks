package com.airbnb.mvrx

import com.airbnb.mvrx.mock.MockBehavior
import com.airbnb.mvrx.mock.MockMvRxViewModelConfigFactory
import com.airbnb.mvrx.mock.MvRxMocks
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ScriptableStateStoreTest : BaseTest() {

    private lateinit var viewModel: TestViewModel

    @Before
    fun setup() {
        val configFactory = MockMvRxViewModelConfigFactory(null).apply {
            mockBehavior = MockBehavior(
                initialStateMocking = MockBehavior.InitialStateMocking.None,
                blockExecutions = MvRxViewModelConfig.BlockExecutions.No,
                stateStoreBehavior = MockBehavior.StateStoreBehavior.Scriptable
            )
        }

        viewModel = TestViewModel(configFactory = configFactory)
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
        MvRxMocks.setScriptableState(viewModel, TestState(foo = 2))
        withState(viewModel) {
            Assert.assertEquals(2, it.foo)
        }
    }

    data class TestState(val foo: Int = 1) : MvRxState

    private class TestViewModel(
        initialState: TestState = TestState(),
        configFactory: MvRxViewModelConfigFactory
    ) : BaseMvRxViewModel<TestState>(
        initialState,
        configFactory
    ) {

        fun attemptToChangeState(newFoo: Int) {
            setState { copy(foo = newFoo) }
        }
    }

}
