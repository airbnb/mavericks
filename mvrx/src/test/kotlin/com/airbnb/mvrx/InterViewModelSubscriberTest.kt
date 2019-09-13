package com.airbnb.mvrx

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

data class OuterViewModelTestState(
    val foo: Int = 0,
    val async: Async<String> = Uninitialized
) : MvRxState

class OuterViewModelTestViewModel(initialState: OuterViewModelTestState) : TestMvRxViewModel<OuterViewModelTestState>(initialState) {

    fun setFoo(foo: Int) = setState { copy(foo = foo) }

    fun setAsync(async: Async<String>) {
        setState { copy(async = async) }
    }

    fun triggerCleared() {
        onCleared()
    }
}

data class InnerViewModelTestState(
    val foo: Int = 0
) : MvRxState

class InnerViewModelTestViewModel(
    initialState: InnerViewModelTestState,
    outerViewModelTestViewModel: OuterViewModelTestViewModel
) : TestMvRxViewModel<InnerViewModelTestState>(initialState) {

    var subscribeCalled = 0
    var selectSubscribeCalled = 0
    var onSuccessCalled = 0
    var onFailCalled = 0

    init {
        subscribe(outerViewModelTestViewModel) { subscribeCalled++ }
        selectSubscribe(outerViewModelTestViewModel, OuterViewModelTestState::foo) { selectSubscribeCalled++ }
        asyncSubscribe(outerViewModelTestViewModel, OuterViewModelTestState::async, { onFailCalled++ }) { onSuccessCalled++ }
    }

    fun triggerCleared() {
        onCleared()
    }
}

class InnerViewModelSubscriberTest : BaseTest() {

    private lateinit var innerViewModel: InnerViewModelTestViewModel
    private lateinit var outerViewModel: OuterViewModelTestViewModel

    @Before
    fun setup() {
        outerViewModel = OuterViewModelTestViewModel(OuterViewModelTestState())
        innerViewModel = InnerViewModelTestViewModel(InnerViewModelTestState(), outerViewModel)
    }

    @Test
    fun testSubscribe() {
        assertEquals(1, innerViewModel.subscribeCalled)
    }

    @Test
    fun testSelectSubscribe() {
        assertEquals(1, innerViewModel.selectSubscribeCalled)
    }

    @Test
    fun testNotChangingFoo() {
        outerViewModel.setFoo(0)
        assertEquals(1, innerViewModel.subscribeCalled)
        assertEquals(0, innerViewModel.onSuccessCalled)
        assertEquals(0, innerViewModel.onFailCalled)
    }

    @Test
    fun testChangingFoo() {
        outerViewModel.setFoo(1)
        assertEquals(2, innerViewModel.subscribeCalled)
        assertEquals(0, innerViewModel.onSuccessCalled)
        assertEquals(0, innerViewModel.onFailCalled)
    }

    @Test
    fun testSuccess() {
        outerViewModel.setAsync(Success("Hello World"))
        assertEquals(2, innerViewModel.subscribeCalled)
        assertEquals(1, innerViewModel.onSuccessCalled)
    }

    @Test
    fun testFail() {
        outerViewModel.setAsync(Fail(IllegalStateException("foo")))
        assertEquals(2, innerViewModel.subscribeCalled)
        assertEquals(0, innerViewModel.onSuccessCalled)
        assertEquals(1, innerViewModel.onFailCalled)
    }

    @Test
    fun testChangesAfterInnerCleared() {
        innerViewModel.triggerCleared()
        outerViewModel.setAsync(Success("Hello World"))
        outerViewModel.setFoo(1)
        assertEquals(1, innerViewModel.subscribeCalled)
        assertEquals(0, innerViewModel.onSuccessCalled)
        assertEquals(0, innerViewModel.onFailCalled)
    }

    @Test
    fun testChangesAfterOuterCleared() {
        outerViewModel.triggerCleared()
        outerViewModel.setFoo(1)
        outerViewModel.setAsync(Success("Hello World"))
        assertEquals(1, innerViewModel.subscribeCalled)
        assertEquals(0, innerViewModel.onSuccessCalled)
        assertEquals(0, innerViewModel.onFailCalled)
    }
}