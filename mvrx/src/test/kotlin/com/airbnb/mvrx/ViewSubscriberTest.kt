package com.airbnb.mvrx

import android.os.Bundle
import org.junit.Assert.assertEquals
import org.junit.Test

data class ViewSubscriberState(val foo: Int = 0) : MvRxState

class ViewSubscriberViewModel(initialState: ViewSubscriberState) : TestMvRxViewModel<ViewSubscriberState>(initialState) {
    fun setFoo(foo: Int) = setState { copy(foo = foo) }
}


class ViewSubscriberFragment : BaseMvRxFragment() {
    private val viewModel: ViewSubscriberViewModel by fragmentViewModel()

    var subscribeCallCount = 0
    var selectSubscribeCalled = -1
    var invalidateCallCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.subscribe { _ -> subscribeCallCount++ }
        viewModel.selectSubscribe(ViewSubscriberState::foo) {
            selectSubscribeCalled = it
        }
    }

    fun setFoo(foo: Int) = viewModel.setFoo(foo)

    override fun invalidate() { invalidateCallCount ++ }
}

class ViewSubscriberTest : BaseTest() {
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
    fun testSelectSubscribe() {
        val (_, fragment) = createFragment<ViewSubscriberFragment, TestActivity>()
        assertEquals(0, fragment.selectSubscribeCalled)
        fragment.setFoo(0)
        assertEquals(0, fragment.selectSubscribeCalled)
        fragment.setFoo(1)
        assertEquals(1, fragment.selectSubscribeCalled)
        assertEquals(1, fragment.selectSubscribeCalled)
    }

    @Test
    fun invalidateCalledFromCreateToStart() {
        val (_, fragment) = createFragment<ViewSubscriberFragment, TestActivity>()
        assertEquals(1, fragment.invalidateCallCount)
    }

    @Test
    fun invalidateCalledFromStopToStartWhenStateChanged() {
        val (controller, fragment) = createFragment<ViewSubscriberFragment, TestActivity>()
        assertEquals(1, fragment.invalidateCallCount)

        controller.stop()

        fragment.setFoo(1)
        assertEquals(1, fragment.invalidateCallCount)

        controller.start()

        assertEquals(2, fragment.invalidateCallCount)
    }

    @Test
    fun invalidateCalledFromStopToStartWhenStateNotChanged() {
        val (controller, fragment) = createFragment<ViewSubscriberFragment, TestActivity>()
        assertEquals(1, fragment.invalidateCallCount)

        controller.stop()

        controller.start()

        assertEquals(2, fragment.invalidateCallCount)
    }
}