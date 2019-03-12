package com.airbnb.mvrx

import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.junit.Assert.assertEquals
import org.junit.Test
import org.robolectric.Robolectric
import java.lang.IllegalStateException

data class ViewSubscriberState(val foo: Int = 0) : MvRxState

class ViewSubscriberViewModel(initialState: ViewSubscriberState) : TestMvRxViewModel<ViewSubscriberState>(initialState) {
    fun setFoo(foo: Int) = setState { copy(foo = foo) }
}


open class ViewSubscriberFragment : BaseMvRxFragment() {
    private val viewModel: ViewSubscriberViewModel by fragmentViewModel()

    var subscribeCallCount = 0
    var subscribeUniqueOnlyCallCount = 0

    var selectSubscribeValue = -1
    var selectSubscribeCallCount = 0

    var selectSubscribeUniqueOnlyValue = -1
    var selectSubscribeUniqueOnlyCallCount = 0

    var invalidateCallCount = 0

    var viewCreatedSubscribeCallCount = 0
    var viewCreatedUniqueOnlyCallCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.subscribe { _ -> subscribeCallCount++ }
        viewModel.subscribe(deliveryMode = uniqueOnly("onCreate")) { _ -> subscribeUniqueOnlyCallCount++ }

        viewModel.selectSubscribe(ViewSubscriberState::foo) {
            selectSubscribeValue = it
            selectSubscribeCallCount++
        }
        viewModel.selectSubscribe(ViewSubscriberState::foo, deliveryMode = uniqueOnly("onCreate")) {
            selectSubscribeUniqueOnlyValue = it
            selectSubscribeUniqueOnlyCallCount++
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate an arbitrary view so onViewCreated is called.
        return inflater.inflate(R.layout.abc_action_bar_title_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.subscribe { _ ->  viewCreatedSubscribeCallCount ++ }
        viewModel.subscribe(deliveryMode = uniqueOnly("onCreateView")) { _ ->  viewCreatedUniqueOnlyCallCount ++ }
    }

    fun setFoo(foo: Int) = viewModel.setFoo(foo)

    override fun invalidate() {
        invalidateCallCount++
    }
}

class FragmentSubscriberTest : BaseTest() {

    private inline fun <reified T : Fragment> createFragmentInTestActivity() = createFragment<T, TestActivity>(containerId = CONTAINER_ID)

    @Test
    fun testSubscribe() {
        val (_, fragment) = createFragmentInTestActivity<ViewSubscriberFragment>()
        assertEquals(1, fragment.subscribeCallCount)
        assertEquals(1, fragment.viewCreatedSubscribeCallCount)

        // No change in state does not trigger update.
        fragment.setFoo(0)
        assertEquals(1, fragment.subscribeCallCount)
        assertEquals(1, fragment.viewCreatedSubscribeCallCount)

        fragment.setFoo(1)
        assertEquals(2, fragment.subscribeCallCount)
        assertEquals(2, fragment.viewCreatedSubscribeCallCount)
    }

    @Test
    fun testSubscribeUniqueOnly() {
        val (_, fragment) = createFragmentInTestActivity<ViewSubscriberFragment>()
        assertEquals(1, fragment.subscribeUniqueOnlyCallCount)
        assertEquals(1, fragment.viewCreatedUniqueOnlyCallCount)

        // No change in state does not trigger update.
        fragment.setFoo(0)
        assertEquals(1, fragment.subscribeUniqueOnlyCallCount)
        assertEquals(1, fragment.viewCreatedUniqueOnlyCallCount)

        fragment.setFoo(1)
        assertEquals(2, fragment.subscribeUniqueOnlyCallCount)
        assertEquals(2, fragment.viewCreatedUniqueOnlyCallCount)
    }

    @Test
    fun testSelectSubscribe() {
        val (_, fragment) = createFragmentInTestActivity<ViewSubscriberFragment>()
        assertEquals(0, fragment.selectSubscribeValue)
        assertEquals(0, fragment.selectSubscribeUniqueOnlyValue)

        // No change in state does not trigger update.
        fragment.setFoo(0)
        assertEquals(0, fragment.selectSubscribeValue)
        assertEquals(0, fragment.selectSubscribeUniqueOnlyValue)

        fragment.setFoo(1)
        assertEquals(1, fragment.selectSubscribeValue)
        assertEquals(1, fragment.selectSubscribeUniqueOnlyValue)
    }

    @Test
    fun testSelectSubscribeUniqueOnly() {
        val (_, fragment) = createFragmentInTestActivity<ViewSubscriberFragment>()
        assertEquals(0, fragment.selectSubscribeUniqueOnlyValue)

        // No change in state does not trigger update.
        fragment.setFoo(0)
        assertEquals(0, fragment.selectSubscribeUniqueOnlyValue)

        fragment.setFoo(1)
        assertEquals(1, fragment.selectSubscribeUniqueOnlyValue)
    }

    @Test
    fun invalidateCalledFromCreateToStart() {
        val (_, fragment) = createFragmentInTestActivity<ViewSubscriberFragment>()
        assertEquals(1, fragment.invalidateCallCount)
    }

    @Test
    fun selectSubscribeFromStopToStartWhenStateChanged() {
        val (controller, fragment) = createFragmentInTestActivity<ViewSubscriberFragment>()
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
        val (controller, fragment) = createFragmentInTestActivity<ViewSubscriberFragment>()
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
        assertEquals(1, fragment.selectSubscribeUniqueOnlyCallCount)
    }

    @Test
    fun subscribeFromStopToStartWhenStateChanged() {
        val (controller, fragment) = createFragmentInTestActivity<ViewSubscriberFragment>()
        assertEquals(1, fragment.subscribeCallCount)
        assertEquals(1, fragment.viewCreatedSubscribeCallCount)

        assertEquals(1, fragment.subscribeUniqueOnlyCallCount)
        assertEquals(1, fragment.viewCreatedUniqueOnlyCallCount)

        controller.pause()
        controller.stop()

        fragment.setFoo(1)
        assertEquals(1, fragment.subscribeCallCount)
        assertEquals(1, fragment.viewCreatedSubscribeCallCount)

        assertEquals(1, fragment.subscribeUniqueOnlyCallCount)
        assertEquals(1, fragment.viewCreatedUniqueOnlyCallCount)

        controller.start()
        controller.resume()

        assertEquals(2, fragment.subscribeCallCount)
        assertEquals(2, fragment.viewCreatedSubscribeCallCount)

        assertEquals(2, fragment.subscribeUniqueOnlyCallCount)
        assertEquals(2, fragment.viewCreatedUniqueOnlyCallCount)
    }

    @Test
    fun subscribeFromStopToStartWhenStateNotChanged() {
        val (controller, fragment) = createFragmentInTestActivity<ViewSubscriberFragment>()
        assertEquals(1, fragment.subscribeCallCount)
        assertEquals(1, fragment.subscribeUniqueOnlyCallCount)

        assertEquals(1, fragment.viewCreatedSubscribeCallCount)
        assertEquals(1, fragment.viewCreatedUniqueOnlyCallCount)

        controller.pause()
        controller.stop()

        assertEquals(1, fragment.subscribeCallCount)
        assertEquals(1, fragment.subscribeUniqueOnlyCallCount)

        assertEquals(1, fragment.viewCreatedSubscribeCallCount)
        assertEquals(1, fragment.viewCreatedUniqueOnlyCallCount)

        controller.start()
        controller.resume()

        assertEquals(2, fragment.subscribeCallCount)
        assertEquals(1, fragment.subscribeUniqueOnlyCallCount)
    }

    @Test
    fun subscribeOnBackStackResumeWhenStateNotChanged() {
        val (controller, fragment) = createFragmentInTestActivity<ViewSubscriberFragment>()
        assertEquals(1, fragment.subscribeCallCount)
        assertEquals(1, fragment.subscribeUniqueOnlyCallCount)

        assertEquals(1, fragment.viewCreatedSubscribeCallCount)
        assertEquals(1, fragment.viewCreatedUniqueOnlyCallCount)

        val fragmentManager = controller.get().supportFragmentManager
        fragmentManager.beginTransaction().replace(CONTAINER_ID, Fragment(), "TAG").addToBackStack(null).commit()

        assertEquals(1, fragment.subscribeCallCount)
        assertEquals(1, fragment.subscribeUniqueOnlyCallCount)

        assertEquals(1, fragment.viewCreatedSubscribeCallCount)
        assertEquals(1, fragment.viewCreatedUniqueOnlyCallCount)

        fragmentManager.popBackStackImmediate()

        assertEquals(2, fragment.subscribeCallCount)
        assertEquals(1, fragment.subscribeUniqueOnlyCallCount)

        assertEquals(2, fragment.viewCreatedSubscribeCallCount)
        assertEquals(1, fragment.viewCreatedUniqueOnlyCallCount)
    }

    @Test
    fun subscribeOnBackStackResumeWhenStateChanged() {
        val (controller, fragment) = createFragmentInTestActivity<ViewSubscriberFragment>()
        assertEquals(1, fragment.subscribeCallCount)
        assertEquals(1, fragment.subscribeUniqueOnlyCallCount)

        assertEquals(1, fragment.viewCreatedSubscribeCallCount)
        assertEquals(1, fragment.viewCreatedUniqueOnlyCallCount)

        val fragmentManager = controller.get().supportFragmentManager
        fragmentManager.beginTransaction().replace(CONTAINER_ID, Fragment(), "TAG").addToBackStack(null).commit()

        // State updates should be paused
        fragment.setFoo(1)
        assertEquals(1, fragment.subscribeCallCount)
        assertEquals(1, fragment.subscribeUniqueOnlyCallCount)

        assertEquals(1, fragment.viewCreatedSubscribeCallCount)
        assertEquals(1, fragment.viewCreatedUniqueOnlyCallCount)

        fragmentManager.popBackStackImmediate()

        assertEquals(2, fragment.subscribeCallCount)
        assertEquals(2, fragment.subscribeUniqueOnlyCallCount)

        assertEquals(2, fragment.viewCreatedSubscribeCallCount)
        assertEquals(2, fragment.viewCreatedUniqueOnlyCallCount)
    }

    /**
     * Robolectric always calls paused during it's configuration change. So we have no good place to dispatch
     * a start change while locked, as calling onPause will unlock the subscriptions. We can achieve this
     * by changing the state in onStop.
     */
    class FragmentWithStateChangeDuringOrientationChange : ViewSubscriberFragment() {

        override fun onStop() {
            super.onStop()
            val subscribeCallCountBeforeFooChange = subscribeCallCount
            val subscribeUniqueOnlyCallCountBeforeFooChange = subscribeUniqueOnlyCallCount
            this.setFoo(1)
            assertEquals(subscribeCallCountBeforeFooChange, subscribeCallCount)
            assertEquals(subscribeUniqueOnlyCallCountBeforeFooChange, subscribeUniqueOnlyCallCount)
        }
    }


    @Test
    fun selectSubscribeOnConfigurationChangeWhenStateChanged() {
        val (controller, fragment) = createFragmentInTestActivity<FragmentWithStateChangeDuringOrientationChange>()
        assertEquals(0, fragment.selectSubscribeValue)
        assertEquals(1, fragment.selectSubscribeCallCount)

        assertEquals(0, fragment.selectSubscribeUniqueOnlyValue)
        assertEquals(1, fragment.selectSubscribeUniqueOnlyCallCount)

        // This will set foo to 1. See FragmentWithStateChangeDuringOrientationChange.
        controller.configurationChange(Configuration().apply {
            setToDefaults()
            this.orientation = Configuration.ORIENTATION_LANDSCAPE
        })

        val recreatedFragment = controller.mvRxFragment<FragmentWithStateChangeDuringOrientationChange>()

        // If unique is not set to true, we always receive an update when unlocked.
        assertEquals(1, recreatedFragment.selectSubscribeValue)
        assertEquals(1, recreatedFragment.selectSubscribeCallCount)

        // Even if unique is true, if the state changes we expect a value.
        assertEquals(1, recreatedFragment.selectSubscribeUniqueOnlyValue)
        assertEquals(1, recreatedFragment.selectSubscribeUniqueOnlyCallCount)
    }

    @Test
    fun selectSubscribeOnConfigurationChangeWhenStateNotChanged() {
        val (controller, fragment) = createFragmentInTestActivity<ViewSubscriberFragment>()
        assertEquals(0, fragment.selectSubscribeValue)
        assertEquals(1, fragment.selectSubscribeCallCount)

        assertEquals(0, fragment.selectSubscribeUniqueOnlyValue)
        assertEquals(1, fragment.selectSubscribeUniqueOnlyCallCount)

        controller.configurationChange(Configuration().apply {
            setToDefaults()
            this.orientation = Configuration.ORIENTATION_LANDSCAPE
        })

        val recreatedFragment = controller.mvRxFragment<ViewSubscriberFragment>()

        // If unique is not set to true, we always receive an update when unlocked.
        assertEquals(0, recreatedFragment.selectSubscribeValue)
        assertEquals(1, recreatedFragment.selectSubscribeCallCount)

        assertEquals(-1, recreatedFragment.selectSubscribeUniqueOnlyValue)
        assertEquals(0, recreatedFragment.selectSubscribeUniqueOnlyCallCount)
    }

    @Test
    fun subscribeOnConfigurationChangeWhenStateChanged() {
        val (controller, fragment) = createFragmentInTestActivity<FragmentWithStateChangeDuringOrientationChange>()
        assertEquals(1, fragment.subscribeCallCount)
        assertEquals(1, fragment.subscribeUniqueOnlyCallCount)

        assertEquals(1, fragment.viewCreatedSubscribeCallCount)
        assertEquals(1, fragment.viewCreatedUniqueOnlyCallCount)

        // This will set foo to 1. See FragmentWithStateChangeDuringOrientationChange.
        controller.configurationChange(Configuration().apply {
            setToDefaults()
            this.orientation = Configuration.ORIENTATION_LANDSCAPE
        })

        val recreatedFragment = controller.mvRxFragment<FragmentWithStateChangeDuringOrientationChange>()

        // As the value changed, the unique only subscription will be called.
        assertEquals(1, recreatedFragment.subscribeCallCount)
        assertEquals(1, recreatedFragment.subscribeUniqueOnlyCallCount)

        assertEquals(1, recreatedFragment.viewCreatedSubscribeCallCount)
        assertEquals(1, recreatedFragment.viewCreatedUniqueOnlyCallCount)
    }

    @Test
    fun subscribeOnConfigurationChangeWhenStateNotChanged() {
        val (controller, fragment) = createFragmentInTestActivity<ViewSubscriberFragment>()
        assertEquals(1, fragment.subscribeCallCount)
        assertEquals(1, fragment.subscribeUniqueOnlyCallCount)

        assertEquals(1, fragment.viewCreatedSubscribeCallCount)
        assertEquals(1, fragment.viewCreatedSubscribeCallCount)

        controller.configurationChange(Configuration().apply {
            setToDefaults()
            this.orientation = Configuration.ORIENTATION_LANDSCAPE
        })

        val recreatedFragment = controller.mvRxFragment<ViewSubscriberFragment>()

        // As the value has not changed, the unique only subscription will not be called.
        assertEquals(1, recreatedFragment.subscribeCallCount)
        assertEquals(0, recreatedFragment.subscribeUniqueOnlyCallCount)

        assertEquals(1, recreatedFragment.viewCreatedSubscribeCallCount)
        assertEquals(0, recreatedFragment.viewCreatedUniqueOnlyCallCount)
    }

    @Test
    fun invalidateCalledFromStopToStartWhenStateChanged() {
        val (controller, fragment) = createFragmentInTestActivity<ViewSubscriberFragment>()
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
        val (controller, fragment) = createFragmentInTestActivity<ViewSubscriberFragment>()
        assertEquals(1, fragment.invalidateCallCount)

        controller.pause()
        controller.stop()

        assertEquals(1, fragment.invalidateCallCount)

        controller.start()
        controller.resume()

        assertEquals(2, fragment.invalidateCallCount)
    }

    class DuplicateUniqueSubscriberFragment : BaseMvRxFragment() {
        private val viewModel: ViewSubscriberViewModel by fragmentViewModel()

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            viewModel.subscribe(deliveryMode = uniqueOnly()) {  }
            viewModel.subscribe(deliveryMode = uniqueOnly()) {  }
        }

        override fun invalidate() { }
    }

    @Test(expected = IllegalStateException::class)
    fun duplicateUniqueOnlySubscribeThrowIllegalStateException() {
         createFragment<DuplicateUniqueSubscriberFragment, TestActivity>(containerId = CONTAINER_ID)
    }
}
//
//class PostCreateFragmentSubscriberTest : BaseTest() {
//
//    @Test
//    fun viewRecreationDoesNotCauseDoubleSubscription() {
//        val (controller, fragment) = createFragment<PostCreateSubscriberFragment, TestActivity>(containerId = CONTAINER_ID)
//        assertEquals(1, fragment.subscribeCallCount)
//
//        val activity = controller.get()
//        val activityFragmentManager = activity.supportFragmentManager!!
//
//        fragment.setFoo(1)
//        assertEquals(2, fragment.subscribeCallCount)
//
//        controller.configurationChange(Configuration().apply {
//            setToDefaults()
//            this.orientation = Configuration.ORIENTATION_LANDSCAPE
//        })
//
//        val recreatedFragment = controller.mvRxFragment<PostCreateSubscriberFragment>()
//        assertEquals(1, recreatedFragment.subscribeCallCount)
//        fragment.setFoo(2)
//        assertEquals(2, recreatedFragment.subscribeCallCount)
//
//    }
//
//}