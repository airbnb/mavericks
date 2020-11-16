package com.airbnb.mvrx.mocking

import androidx.fragment.app.Fragment
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.existingViewModel
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.parentFragmentViewModel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.robolectric.android.controller.ActivityController

class ViewModelDelegateProviderTest : BaseTest() {
    private inline fun <reified T : Fragment> createFragment(
        existingController: ActivityController<TestActivity>? = null
    ): Pair<ActivityController<TestActivity>, T> {
        return createFragment(
            containerId = CONTAINER_ID,
            existingController = existingController
        )
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
        val mockVariants = mockVariants<Frag>()

        val mockBehavior = MockBehavior(
            initialStateMocking = MockBehavior.InitialStateMocking.Full,
            stateStoreBehavior = MockBehavior.StateStoreBehavior.Scriptable
        )

        val mockedView = mockVariants.forDefaultState().createView(mockBehavior)

        val frag = mockedView.viewInstance
        frag.addToActivity<Frag, TestActivity>()

        assertEquals(TestState(1), frag.fragmentVm.state())
        assertEquals(TestState(2), frag.activityVm.state())
    }

    @Test
    fun existingViewModelWithMockedState() {
        val mockVariants = getMockVariants<Frag2, Nothing>(viewProvider = { _, _ ->
            Frag2()
        })

        checkNotNull(mockVariants)

        val mockBehavior = MockBehavior(
            initialStateMocking = MockBehavior.InitialStateMocking.Full,
            stateStoreBehavior = MockBehavior.StateStoreBehavior.Scriptable
        )

        val mockedView = mockVariants.first { it.mock.isDefaultState }.createView(mockBehavior)

        val frag = mockedView.viewInstance
        frag.addToActivity<Frag2, TestActivity>()

        assertEquals(TestState(2), frag.existingVm.state())
    }

    @Test
    fun parentFragmentViewModelWithMockedState() {
        val mockVariants = getMockVariants<Frag3, Nothing>(viewProvider = { _, _ ->
            Frag3()
        })

        checkNotNull(mockVariants)

        val mockBehavior = MockBehavior(
            initialStateMocking = MockBehavior.InitialStateMocking.Full,
            stateStoreBehavior = MockBehavior.StateStoreBehavior.Scriptable
        )

        val mockedView = mockVariants.first { it.mock.isDefaultState }.createView(mockBehavior)

        val frag = mockedView.viewInstance
        frag.addToActivity<Frag3, TestActivity>()

        assertEquals(TestState(2), frag.parentFragmentVM.state())
    }

    class Frag : Fragment(), MockableMavericksView {
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

    class Frag2 : Fragment(), MockableMavericksView {
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

    class Frag3 : Fragment(), MockableMavericksView {
        val parentFragmentVM: ActivityVM by parentFragmentViewModel()

        override fun invalidate() {
        }

        override fun provideMocks() = mockSingleViewModel(
            viewModelReference = Frag3::parentFragmentVM,
            defaultState = TestState(2),
            defaultArgs = null
        ) {

        }
    }

    data class TestState(val num: Int = 0) : MavericksState
    class FragmentVM(initialState: TestState) :
        MavericksViewModel<TestState>(initialState)

    class ActivityVM(initialState: TestState) :
        MavericksViewModel<TestState>(initialState)
}
