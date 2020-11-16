package com.airbnb.mvrx

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

data class OuterViewModelTestState(
    val foo: Int = 0,
    val prop2: Int = 0,
    val prop3: Int = 0,
    val prop4: Int = 0,
    val prop5: Int = 0,
    val prop6: Int = 0,
    val prop7: Int = 0,
    val async: Async<String> = Uninitialized
) : MavericksState

class OuterViewModelTestViewModel(initialState: OuterViewModelTestState) : BaseMvRxViewModel<OuterViewModelTestState>(initialState) {

    fun setFoo(foo: Int) = setState { copy(foo = foo) }

    fun setProp2(value: Int) = setState { copy(prop2 = value) }

    fun setProp3(value: Int) = setState { copy(prop3 = value) }

    fun setProp4(value: Int) = setState { copy(prop4 = value) }

    fun setProp5(value: Int) = setState { copy(prop5 = value) }

    fun setProp6(value: Int) = setState { copy(prop6 = value) }

    fun setProp7(value: Int) = setState { copy(prop7 = value) }

    fun setAsync(async: Async<String>) {
        setState { copy(async = async) }
    }

    fun triggerCleared() {
        onCleared()
    }

    fun subscribeSameViewModel() {
        subscribe(this) { }
    }

    fun asyncSubscribeSameViewModel() {
        asyncSubscribe(this, OuterViewModelTestState::async) { }
    }

    fun selectSubscribeSameViewModel() {
        selectSubscribe(this, OuterViewModelTestState::foo) { }
    }

    fun subscribeToTwoPropsSameViewModel() {
        selectSubscribe(this, OuterViewModelTestState::foo, OuterViewModelTestState::prop2) { _, _ -> }
    }

    fun subscribeToThreePropsSameViewModel() {
        selectSubscribe(
            this,
            OuterViewModelTestState::foo,
            OuterViewModelTestState::prop2,
            OuterViewModelTestState::prop3
        ) { _, _, _ -> }
    }

    fun subscribeToFourPropsSameViewModel() {
        selectSubscribe(
            this,
            OuterViewModelTestState::foo,
            OuterViewModelTestState::prop2,
            OuterViewModelTestState::prop3,
            OuterViewModelTestState::prop4
        ) { _, _, _, _ -> }
    }

    fun subscribeToFivePropsSameViewModel() {
        selectSubscribe(
            this,
            OuterViewModelTestState::foo,
            OuterViewModelTestState::prop2,
            OuterViewModelTestState::prop3,
            OuterViewModelTestState::prop4,
            OuterViewModelTestState::prop5
        ) { _, _, _, _, _ -> }
    }

    fun subscribeToSixPropsSameViewModel() {
        selectSubscribe(
            this,
            OuterViewModelTestState::foo,
            OuterViewModelTestState::prop2,
            OuterViewModelTestState::prop3,
            OuterViewModelTestState::prop4,
            OuterViewModelTestState::prop5,
            OuterViewModelTestState::prop6
        ) { _, _, _, _, _, _ -> }
    }

    fun subscribeToSevenPropsSameViewModel() {
        selectSubscribe(
            this,
            OuterViewModelTestState::foo,
            OuterViewModelTestState::prop2,
            OuterViewModelTestState::prop3,
            OuterViewModelTestState::prop4,
            OuterViewModelTestState::prop5,
            OuterViewModelTestState::prop6,
            OuterViewModelTestState::prop7
        ) { _, _, _, _, _, _, _ -> }
    }
}

data class InnerViewModelTestState(
    val foo: Int = 0
) : MavericksState

class InnerViewModelTestViewModel(
    initialState: InnerViewModelTestState,
    outerViewModelTestViewModel: OuterViewModelTestViewModel
) : BaseMvRxViewModel<InnerViewModelTestState>(initialState) {

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

    fun subscribeToTwoProps(viewModel: OuterViewModelTestViewModel, subscriber: () -> Unit) {
        selectSubscribe(viewModel, OuterViewModelTestState::foo, OuterViewModelTestState::prop2) { _, _ -> subscriber.invoke() }
    }

    fun subscribeToThreeProps(viewModel: OuterViewModelTestViewModel, subscriber: () -> Unit) {
        selectSubscribe(
            viewModel,
            OuterViewModelTestState::foo,
            OuterViewModelTestState::prop2,
            OuterViewModelTestState::prop3
        ) { _, _, _ -> subscriber.invoke() }
    }

    fun subscribeToFourProps(viewModel: OuterViewModelTestViewModel, subscriber: () -> Unit) {
        selectSubscribe(
            viewModel,
            OuterViewModelTestState::foo,
            OuterViewModelTestState::prop2,
            OuterViewModelTestState::prop3,
            OuterViewModelTestState::prop4
        ) { _, _, _, _ -> subscriber.invoke() }
    }

    fun subscribeToFiveProps(viewModel: OuterViewModelTestViewModel, subscriber: () -> Unit) {
        selectSubscribe(
            viewModel,
            OuterViewModelTestState::foo,
            OuterViewModelTestState::prop2,
            OuterViewModelTestState::prop3,
            OuterViewModelTestState::prop4,
            OuterViewModelTestState::prop5
        ) { _, _, _, _, _ -> subscriber.invoke() }
    }

    fun subscribeToSixProps(viewModel: OuterViewModelTestViewModel, subscriber: () -> Unit) {
        selectSubscribe(
            viewModel,
            OuterViewModelTestState::foo,
            OuterViewModelTestState::prop2,
            OuterViewModelTestState::prop3,
            OuterViewModelTestState::prop4,
            OuterViewModelTestState::prop5,
            OuterViewModelTestState::prop6
        ) { _, _, _, _, _, _ -> subscriber.invoke() }
    }

    fun subscribeToSevenProps(viewModel: OuterViewModelTestViewModel, subscriber: () -> Unit) {
        selectSubscribe(
            viewModel,
            OuterViewModelTestState::foo,
            OuterViewModelTestState::prop2,
            OuterViewModelTestState::prop3,
            OuterViewModelTestState::prop4,
            OuterViewModelTestState::prop5,
            OuterViewModelTestState::prop6,
            OuterViewModelTestState::prop7
        ) { _, _, _, _, _, _, _ -> subscriber.invoke() }
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
    fun testSelectSubscribe2() {
        var callCount = 0
        innerViewModel.subscribeToTwoProps(outerViewModel) { callCount++ }
        assertEquals(1, callCount)
        outerViewModel.setFoo(1)
        assertEquals(2, callCount)
        outerViewModel.setProp2(2)
        assertEquals(3, callCount)
    }

    @Test
    fun testSelectSubscribe3() {
        var callCount = 0
        innerViewModel.subscribeToThreeProps(outerViewModel) { callCount++ }
        assertEquals(1, callCount)
        outerViewModel.setFoo(1)
        assertEquals(2, callCount)
        outerViewModel.setProp2(2)
        assertEquals(3, callCount)
        outerViewModel.setProp3(3)
        assertEquals(4, callCount)
    }

    @Test
    fun testSelectSubscribe4() {
        var callCount = 0
        innerViewModel.subscribeToFourProps(outerViewModel) { callCount++ }
        assertEquals(1, callCount)
        outerViewModel.setFoo(1)
        assertEquals(2, callCount)
        outerViewModel.setProp2(2)
        assertEquals(3, callCount)
        outerViewModel.setProp3(3)
        assertEquals(4, callCount)
        outerViewModel.setProp4(4)
        assertEquals(5, callCount)
    }

    @Test
    fun testSelectSubscribe5() {
        var callCount = 0
        innerViewModel.subscribeToFiveProps(outerViewModel) { callCount++ }
        assertEquals(1, callCount)
        outerViewModel.setFoo(1)
        assertEquals(2, callCount)
        outerViewModel.setProp2(2)
        assertEquals(3, callCount)
        outerViewModel.setProp3(3)
        assertEquals(4, callCount)
        outerViewModel.setProp4(4)
        assertEquals(5, callCount)
        outerViewModel.setProp5(5)
        assertEquals(6, callCount)
    }

    @Test
    fun testSelectSubscribe6() {
        var callCount = 0
        innerViewModel.subscribeToSixProps(outerViewModel) { callCount++ }
        assertEquals(1, callCount)
        outerViewModel.setFoo(1)
        assertEquals(2, callCount)
        outerViewModel.setProp2(2)
        assertEquals(3, callCount)
        outerViewModel.setProp3(3)
        assertEquals(4, callCount)
        outerViewModel.setProp4(4)
        assertEquals(5, callCount)
        outerViewModel.setProp5(5)
        assertEquals(6, callCount)
        outerViewModel.setProp6(6)
        assertEquals(7, callCount)
    }

    @Test
    fun testSelectSubscribe7() {
        var callCount = 0
        innerViewModel.subscribeToSevenProps(outerViewModel) { callCount++ }
        assertEquals(1, callCount)
        outerViewModel.setFoo(1)
        assertEquals(2, callCount)
        outerViewModel.setProp2(2)
        assertEquals(3, callCount)
        outerViewModel.setProp3(3)
        assertEquals(4, callCount)
        outerViewModel.setProp4(4)
        assertEquals(5, callCount)
        outerViewModel.setProp5(5)
        assertEquals(6, callCount)
        outerViewModel.setProp6(6)
        assertEquals(7, callCount)
        outerViewModel.setProp7(7)
        assertEquals(8, callCount)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSameViewModelSubscribe() {
        outerViewModel.subscribeSameViewModel()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSameViewModelAsyncSubscribe() {
        outerViewModel.asyncSubscribeSameViewModel()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSameViewModelSelectSubscribe() {
        outerViewModel.selectSubscribeSameViewModel()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSameViewModelSelectSubscribe2() {
        outerViewModel.subscribeToTwoPropsSameViewModel()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSameViewModelSelectSubscribe3() {
        outerViewModel.subscribeToThreePropsSameViewModel()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSameViewModelSelectSubscribe4() {
        outerViewModel.subscribeToFourPropsSameViewModel()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSameViewModelSelectSubscribe5() {
        outerViewModel.subscribeToFivePropsSameViewModel()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSameViewModelSelectSubscribe6() {
        outerViewModel.subscribeToSixPropsSameViewModel()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSameViewModelSelectSubscribe7() {
        outerViewModel.subscribeToSevenPropsSameViewModel()
    }
}
