package com.airbnb.mvrx

import junit.framework.Assert
import org.junit.Test

class MvRxMockerTest : BaseTest() {

    private val initialState = TestState("initial", 0)
    private val mockState = TestState("updated", 1)

    @Test
    fun testDisabled() {
        MvRxMocker.enabled = false

        val viewModel = TestViewModel(initialState)
        MvRxMocker.setMockedState(viewModel, mockState)

        withState(viewModel) { state ->
            Assert.assertEquals(state, initialState)
        }
    }

    @Test
    fun testEnabled() {
        MvRxMocker.enabled = true

        val viewModel = TestViewModel(initialState)
        MvRxMocker.setMockedState(viewModel, mockState)

        withState(viewModel) { state ->
            Assert.assertEquals(mockState, state)
        }
    }

    @Test
    fun testMockNotSet() {
        MvRxMocker.enabled = true

        val viewModel = TestViewModel(initialState)

        withState(viewModel) { state ->
            Assert.assertEquals(initialState, state)
        }
    }

    @Test
    fun testMockArgs() {
        MvRxMocker.enabled = true

        val viewModel = TestViewModel(initialState)
        val args = ParcelableArgs("test args")
        MvRxMocker.setMockedStateFromArgs(viewModel, args)

        withState(viewModel) { state ->
            Assert.assertEquals(TestState(args), state)
        }
    }

    private class TestViewModel(initialState: TestState) : TestMvRxViewModel<TestState>(initialState)
}
