package com.airbnb.mvrx

import android.arch.lifecycle.Lifecycle
import org.junit.Assert.assertEquals
import org.junit.Test

data class SelectSubscribeState(val prop1: String = "Hello", val prop2: String = "World") : MvRxState

class SelectSubscribeViewModel(override val initialState: SelectSubscribeState) : TestMvRxViewModel<SelectSubscribeState>() {
    fun setProp2(value: String) = setState { copy(prop2 = value) }
}

class SelectSubscribeTest : MvRxBaseTest() {

    @Test
    fun testSelectSubscribeCalled() {
        val viewModel = SelectSubscribeViewModel(SelectSubscribeState())
        val owner = TestLifecycleOwner()
        var callCount = 0
        viewModel.selectSubscribe(owner, SelectSubscribeState::prop2) {
            callCount++
        }
        assertEquals(1, callCount)
    }

    @Test
    fun testSelectSubscribeInitialValue() {
        val viewModel = SelectSubscribeViewModel(SelectSubscribeState())
        val owner = TestLifecycleOwner()
        viewModel.selectSubscribe(owner, SelectSubscribeState::prop2) {
            assertEquals("World", it)
        }
    }

    @Test
    fun testSelectSubscribeChanged() {
        val viewModel = SelectSubscribeViewModel(SelectSubscribeState())
        val owner = TestLifecycleOwner()
        var callCount = 0
        viewModel.selectSubscribe(owner, SelectSubscribeState::prop2) {
            if (callCount == 0) assertEquals("World", it)
            else assertEquals("World!", it)
            callCount++
        }
        viewModel.setProp2("World!")
    }

    @Test
    fun testSelectSubscribeNotCalledWhenNotStarted() {
        val viewModel = SelectSubscribeViewModel(SelectSubscribeState())
        val owner = TestLifecycleOwner()
        owner.lifecycle.markState(Lifecycle.State.CREATED)
        var callCount = 0
        viewModel.selectSubscribe(owner, SelectSubscribeState::prop2) {
            callCount++
        }
        assertEquals(0, callCount)
    }
}