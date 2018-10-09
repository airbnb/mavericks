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
    var subscribeUniqueOnlyCallCount = 0

    var selectSubscribeValue = -1
    var selectSubscribeCallCount = 0

    var selectSubscribeUniqueOnlyValue = -1
    var selectSubscribeUniqueOnlyCallCount = 0

    var invalidateCallCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.subscribe { _ -> subscribeCallCount++ }
        viewModel.subscribe(uniqueOnly = true) { _ -> subscribeUniqueOnlyCallCount++ }

        viewModel.selectSubscribe(ViewSubscriberState::foo) {
            selectSubscribeValue = it
            selectSubscribeCallCount++
        }
        viewModel.selectSubscribe(ViewSubscriberState::foo, uniqueOnly = true) {
            selectSubscribeUniqueOnlyValue = it
            selectSubscribeUniqueOnlyCallCount++
        }
    }

    fun setFoo(foo: Int) = viewModel.setFoo(foo)

    override fun invalidate() {
        invalidateCallCount++
    }
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
    fun testSubscribeUniqueOnly() {
        val (_, fragment) = createFragment<ViewSubscriberFragment, TestActivity>()
        assertEquals(1, fragment.subscribeUniqueOnlyCallCount)
        fragment.setFoo(0)
        assertEquals(1, fragment.subscribeUniqueOnlyCallCount)
        fragment.setFoo(1)
        assertEquals(2, fragment.subscribeUniqueOnlyCallCount)
    }

    @Test
    fun testSelectSubscribe() {
        val (_, fragment) = createFragment<ViewSubscriberFragment, TestActivity>()
        assertEquals(0, fragment.selectSubscribeValue)
        assertEquals(0, fragment.selectSubscribeUniqueOnlyValue)
        fragment.setFoo(0)
        assertEquals(0, fragment.selectSubscribeValue)
        assertEquals(0, fragment.selectSubscribeUniqueOnlyValue)
        fragment.setFoo(1)
        assertEquals(1, fragment.selectSubscribeValue)
        assertEquals(1, fragment.selectSubscribeUniqueOnlyValue)
    }

    @Test
    fun testSelectSubscribeUniqueOnly() {
        val (_, fragment) = createFragment<ViewSubscriberFragment, TestActivity>()
        assertEquals(0, fragment.selectSubscribeUniqueOnlyValue)
        fragment.setFoo(0)
        assertEquals(0, fragment.selectSubscribeUniqueOnlyValue)
        fragment.setFoo(1)
        assertEquals(1, fragment.selectSubscribeUniqueOnlyValue)
    }

    @Test
    fun invalidateCalledFromCreateToStart() {
        val (_, fragment) = createFragment<ViewSubscriberFragment, TestActivity>()
        assertEquals(1, fragment.invalidateCallCount)
    }

    @Test
    fun selectSubscribeFromStopToStartWhenStateChanged() {
        val (controller, fragment) = createFragment<ViewSubscriberFragment, TestActivity>()
        assertEquals(0, fragment.selectSubscribeValue)
        assertEquals(0, fragment.selectSubscribeUniqueOnlyValue)

        controller.pause()
        controller.stop()

        fragment.setFoo(1)
        assertEquals(0, fragment.selectSubscribeValue)
        assertEquals(0, fragment.selectSubscribeUniqueOnlyValue)

        controller.start()
        controller.resume()

        assertEquals(1, fragment.selectSubscribeValue)
        assertEquals(1, fragment.selectSubscribeUniqueOnlyValue)
    }

    @Test
    fun selectSubscribeFromStopToStartWhenStateNotChanged() {
        val (controller, fragment) = createFragment<ViewSubscriberFragment, TestActivity>()
        assertEquals(0, fragment.selectSubscribeValue)
        assertEquals(1, fragment.selectSubscribeCallCount)

        assertEquals(0, fragment.selectSubscribeUniqueOnlyValue)
        assertEquals(1, fragment.selectSubscribeUniqueOnlyCallCount)

        controller.pause()
        controller.stop()

        assertEquals(0, fragment.selectSubscribeValue)
        assertEquals(1, fragment.selectSubscribeCallCount)

        assertEquals(0, fragment.selectSubscribeUniqueOnlyValue)
        assertEquals(1, fragment.selectSubscribeUniqueOnlyCallCount)

        controller.start()
        controller.resume()

        // If unique is not set to true, we always receive an update when unlocked.
        assertEquals(0, fragment.selectSubscribeValue)
        assertEquals(2, fragment.selectSubscribeCallCount)

        assertEquals(0, fragment.selectSubscribeUniqueOnlyValue)
        assertEquals(0, fragment.selectSubscribeUniqueOnlyValue)
    }

    @Test
    fun subscribeFromStopToStartWhenStateChanged() {
        val (controller, fragment) = createFragment<ViewSubscriberFragment, TestActivity>()
        assertEquals(1, fragment.subscribeCallCount)
        assertEquals(1, fragment.subscribeUniqueOnlyCallCount)

        controller.pause()
        controller.stop()

        fragment.setFoo(1)
        assertEquals(1, fragment.subscribeCallCount)
        assertEquals(1, fragment.subscribeUniqueOnlyCallCount)

        controller.start()
        controller.resume()

        assertEquals(2, fragment.subscribeCallCount)
        assertEquals(2, fragment.subscribeUniqueOnlyCallCount)
    }

    @Test
    fun subscribeFromStopToStartWhenStateNotChanged() {
        val (controller, fragment) = createFragment<ViewSubscriberFragment, TestActivity>()
        assertEquals(1, fragment.subscribeCallCount)
        assertEquals(1, fragment.subscribeUniqueOnlyCallCount)

        controller.pause()
        controller.stop()

        assertEquals(1, fragment.subscribeCallCount)
        assertEquals(1, fragment.subscribeUniqueOnlyCallCount)

        controller.start()
        controller.resume()

        assertEquals(2, fragment.subscribeCallCount)
        assertEquals(1, fragment.subscribeUniqueOnlyCallCount)
    }

    @Test
    fun invalidateCalledFromStopToStartWhenStateChanged() {
        val (controller, fragment) = createFragment<ViewSubscriberFragment, TestActivity>()
        assertEquals(1, fragment.invalidateCallCount)

        controller.pause()
        controller.stop()

        fragment.setFoo(1)
        assertEquals(1, fragment.invalidateCallCount)

        controller.start()
        controller.resume()

        assertEquals(2, fragment.invalidateCallCount)
    }

    @Test
    fun invalidateCalledFromStopToStartWhenStateNotChanged() {
        val (controller, fragment) = createFragment<ViewSubscriberFragment, TestActivity>()
        assertEquals(1, fragment.invalidateCallCount)

        controller.pause()
        controller.stop()

        assertEquals(1, fragment.invalidateCallCount)

        controller.start()
        controller.resume()

        assertEquals(2, fragment.invalidateCallCount)
    }
}