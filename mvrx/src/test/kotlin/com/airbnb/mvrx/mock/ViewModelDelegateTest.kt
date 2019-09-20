package com.airbnb.mvrx.mock

import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.BaseTest
import com.airbnb.mvrx.CONTAINER_ID
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.RealMvRxStateStore
import com.airbnb.mvrx.TestActivity
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.existingViewModel
import com.airbnb.mvrx.fragmentViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.robolectric.android.controller.ActivityController

class ViewModelDelegateTest : BaseTest() {
    private inline fun <reified T : BaseMvRxFragment> createFragment(
        existingController: ActivityController<TestActivity>? = null
    ): Pair<ActivityController<TestActivity>, T> {
        return createFragment(
            containerId = CONTAINER_ID,
            existingController = existingController
        )
    }

    @Test
    fun fragmentViewModelDelegateIsRealByDefault() {
        val (_, frag) = createFragment<Frag>()
        assertTrue(frag.fragmentVm.config.stateStore is RealMvRxStateStore)
    }

    @Test
    fun activityViewModelDelegateIsRealByDefault() {
        val (_, frag) = createFragment<Frag>()
        assertTrue(frag.activityVm.config.stateStore is RealMvRxStateStore)
    }

    @Test(expected = Exception::class)
    fun existingViewModelDelegateThrowsIfNoViewModelExists() {
        val (_, frag) = createFragment<Frag2>()
        frag.existingVm
    }

    @Test
    fun existingViewModelDelegateInheritsActivityViewModel() {
        val (controller, frag) = createFragment<Frag>()
        val (_, frag2) = createFragment<Frag2>(existingController = controller)
        assertEquals(frag.activityVm, frag2.existingVm)
    }

    @Test
    fun createFragmentWithMockedState() {
        val mockVariants = getMockVariants<Frag, Nothing>(viewProvider = { _, _ ->
            Frag()
        })

        checkNotNull(mockVariants)

        val mockBehavior = MockBehavior(
            initialState = MockBehavior.InitialState.Full,
            blockExecutions = MockBehavior.BlockExecutions.No,
            stateStoreBehavior = MockBehavior.StateStoreBehavior.Scriptable
        )

        val mockedView = mockVariants.first { it.mock.isDefaultState }.createView(mockBehavior)

        val frag = mockedView.viewInstance
        frag.addToActivity<Frag, TestActivity>()

        assertEquals(TestState(1), frag.fragmentVm.state)
        assertEquals(TestState(2), frag.activityVm.state)
    }

    @Test
    fun existingViewModelWithMockedState() {
        val mockVariants = getMockVariants<Frag2, Nothing>(viewProvider = { _, _ ->
            Frag2()
        })

        checkNotNull(mockVariants)

        val mockBehavior = MockBehavior(
            initialState = MockBehavior.InitialState.Full,
            blockExecutions = MockBehavior.BlockExecutions.No,
            stateStoreBehavior = MockBehavior.StateStoreBehavior.Scriptable
        )

        val mockedView = mockVariants.first { it.mock.isDefaultState }.createView(mockBehavior)

        val frag = mockedView.viewInstance
        frag.addToActivity<Frag2, TestActivity>()

        assertEquals(TestState(2), frag.existingVm.state)
    }

    class Frag : BaseMvRxFragment() {
        val fragmentVm: FragmentVM by fragmentViewModel()
        val activityVm: ActivityVM by activityViewModel()

        override fun invalidate() {

        }

        override fun provideMocks() = mockTwoViewModels(
            viewModel1Reference = Frag::fragmentVm,
            defaultState1 = TestState(1),
            viewModel2Reference = Frag::activityVm,
            defaultState2 = TestState(2),
            defaultArgs = null
        ) {

        }
    }

    class Frag2 : BaseMvRxFragment() {
        val existingVm: ActivityVM by existingViewModel()

        override fun invalidate() {

        }

        override fun provideMocks() = mockSingleViewModel(
            viewModelReference = Frag2::existingVm,
            defaultState = TestState(2),
            defaultArgs = null
        ) {

        }
    }

    data class TestState(val num: Int = 0) : MvRxState
    class FragmentVM(initialState: TestState) : BaseMvRxViewModel<TestState>(initialState)
    class ActivityVM(initialState: TestState) : BaseMvRxViewModel<TestState>(initialState)
}




