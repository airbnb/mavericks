package com.airbnb.mvrx

import io.reactivex.Maybe
import io.reactivex.disposables.Disposable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

data class ViewModelTestState(val foo: Int = 0) : MvRxState
class ViewModelTestViewModel(override val initialState: ViewModelTestState) : TestMvRxViewModel<ViewModelTestState>() {

    var subscribeCallCount = 0
    var subscribeWithHistoryCallCount = 0
    var selectSubscribeCalled = 0

    init {
        subscribe { _ -> subscribeCallCount++ }
        subscribeWithHistory { _, _ ->  subscribeWithHistoryCallCount++ }
        selectSubscribe(ViewModelTestState::foo) { selectSubscribeCalled++ }
    }

    fun setFoo(foo: Int) = setState { copy(foo = foo) }

    fun disposeOnClear(disposable: Disposable) {
        disposable.disposeOnClear()
    }

    fun triggerCleared() {
        onCleared()
    }
}

class ViewModelTest : MvRxBaseTest() {

    private lateinit var viewModel: ViewModelTestViewModel

    @Before
    fun setup() {
        viewModel = ViewModelTestViewModel(ViewModelTestState())
    }

    @Test
    fun testSubscribeWithHistory() {
        assertEquals(1, viewModel.subscribeWithHistoryCallCount)
    }

    @Test
    fun testSubscribe() {
        assertEquals(1, viewModel.subscribeCallCount)
    }

    @Test
    fun testSelectSubscribe() {
        assertEquals(1, viewModel.selectSubscribeCalled)
    }

    @Test
    fun testChangeState() {
        assertEquals(1, viewModel.subscribeCallCount)
        assertEquals(0, viewModel.selectSubscribeCalled)
        assertEquals(1, viewModel.subscribeWithHistoryCallCount)
        viewModel.setFoo(0)
        assertEquals(1, viewModel.subscribeCallCount)
        assertEquals(0, viewModel.selectSubscribeCalled)
        assertEquals(1, viewModel.subscribeWithHistoryCallCount)
        viewModel.setFoo(1)
        assertEquals(2, viewModel.subscribeCallCount)
        assertEquals(1, viewModel.selectSubscribeCalled)
        assertEquals(2, viewModel.subscribeWithHistoryCallCount)
    }

    @Test
    fun testDisposeOnClear() {
        val disposable = Maybe.never<Int>().subscribe()
        viewModel.disposeOnClear(disposable)
        assertFalse(disposable.isDisposed)
        viewModel.triggerCleared()
        assertTrue(disposable.isDisposed)
    }
}