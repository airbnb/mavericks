package com.airbnb.mvrx

import android.os.Bundle
import org.junit.Assert.assertEquals
import org.junit.Test

data class ViewSubscriberState(val foo: Int = 0) : MvRxState

class ViewSubscriberViewModel(override val initialState: ViewSubscriberState) : TestMvRxViewModel<ViewSubscriberState>() {
    fun setFoo(foo: Int) = setState { copy(foo = foo) }
}


class ViewSubscriberFragment : BaseMvRxFragment() {
    private val viewModel by fragmentViewModel(ViewSubscriberViewModel::class)

    var subscribeCallCount = 0
    var subscribeWithHistoryCallCount = 0
    var selectSubscribeCalled = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.subscribe { _ -> subscribeCallCount++ }
        viewModel.subscribeWithHistory { _, _ ->  subscribeWithHistoryCallCount++ }
        viewModel.selectSubscribe(ViewSubscriberState::foo) { selectSubscribeCalled++ }
    }

    fun setFoo(foo: Int) = viewModel.setFoo(foo)

    override fun invalidate() {}
}

class ViewSubscriberTest : MvRxBaseTest() {
    @Test
    fun testSubscribe() {
        val (_, fragment) = createFragment<ViewSubscriberFragment, TestActivity>()
        assertEquals(1, fragment.subscribeCallCount)
        fragment.setFoo(0)
        assertEquals(1, fragment.subscribeCallCount)
        fragment.setFoo(1)
        assertEquals(2, fragment.subscribeCallCount)
    }

    @Test
    fun testSubscribeWithHistory() {
        val (_, fragment) = createFragment<ViewSubscriberFragment, TestActivity>()
        assertEquals(1, fragment.subscribeWithHistoryCallCount)
        fragment.setFoo(0)
        assertEquals(1, fragment.subscribeWithHistoryCallCount)
        fragment.setFoo(1)
        assertEquals(2, fragment.subscribeWithHistoryCallCount)
    }

    @Test
    fun testSelectSubscribe() {
        val (_, fragment) = createFragment<ViewSubscriberFragment, TestActivity>()
        assertEquals(0, fragment.selectSubscribeCalled)
        fragment.setFoo(0)
        assertEquals(0, fragment.selectSubscribeCalled)
        fragment.setFoo(1)
        assertEquals(1, fragment.selectSubscribeCalled)
    }
}