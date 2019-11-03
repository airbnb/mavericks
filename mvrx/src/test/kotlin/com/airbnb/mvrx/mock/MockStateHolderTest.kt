package com.airbnb.mvrx.mock

import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.BaseTest
import com.airbnb.mvrx.MvRx
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.MvRxViewModelConfigFactory
import com.airbnb.mvrx.fragmentViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class MockStateHolderTest : BaseTest() {

    @Before
    fun setup() {
        MvRx.viewModelConfigFactory.mockBehavior = MockBehavior(
            initialState = MockBehavior.InitialState.Full,
            stateStoreBehavior = MockBehavior.StateStoreBehavior.Scriptable,
            blockExecutions = MockBehavior.BlockExecutions.No
        )
    }

    @After
    fun tearDown() {
        MvRx.viewModelConfigFactory =
            MvRxViewModelConfigFactory()
    }

    @Test
    fun getDefaultState() {
        val holder = MvRxMocks.mockStateHolder
        val frag = Frag()
        val viewMocks = MvRxViewMocks.getFrom(frag)
        val mockToUse = viewMocks.mocks.first { it.isDefaultState }
        holder.setMock(frag, mockToUse)

        val mockedState = holder.getMockedState(
            view = frag,
            viewModelProperty = Frag::fragmentVm,
            existingViewModel = false,
            stateClass = TestState::class.java,
            forceMockExistingViewModel = false
        )

        assertEquals(mockToUse.states.single().state, mockedState)
    }

    @Test
    fun clearMock() {
        val holder = MvRx.mockStateHolder
        val frag = Frag()
        val viewMocks = MvRxViewMocks.getFrom(frag)
        val mockToUse = viewMocks.mocks.first { it.isDefaultState }
        holder.setMock(frag, mockToUse)

        holder.clearMock(frag)

        val mockedState = holder.getMockedState(
            view = frag,
            viewModelProperty = Frag::fragmentVm,
            existingViewModel = false,
            stateClass = TestState::class.java,
            forceMockExistingViewModel = false
        )

        assertNull(mockedState)
    }

    @Test
    fun clearAllMocks() {
        val holder = MvRx.mockStateHolder
        val frag = Frag()
        val viewMocks = MvRxViewMocks.getFrom(frag)
        val mockToUse = viewMocks.mocks.first { it.isDefaultState }
        holder.setMock(frag, mockToUse)

        holder.clearAllMocks()

        val mockedState = holder.getMockedState(
            view = frag,
            viewModelProperty = Frag::fragmentVm,
            existingViewModel = false,
            stateClass = TestState::class.java,
            forceMockExistingViewModel = false
        )

        assertNull(mockedState)
    }

    @Test
    fun defaultInitializationGivesNullState() {
        val holder = MvRx.mockStateHolder
        val frag = Frag()
        val viewMocks = MvRxViewMocks.getFrom(frag)
        val mockToUse = viewMocks.mocks.first { it.isDefaultInitialization }
        holder.setMock(frag, mockToUse)

        val mockedState = holder.getMockedState(
            view = frag,
            viewModelProperty = Frag::fragmentVm,
            existingViewModel = false,
            stateClass = TestState::class.java,
            forceMockExistingViewModel = false
        )

        assertNull(mockedState)
    }

    @Test
    fun defaultInitializationForExistingViewModelDoesNotGiveNullState() {
        val holder = MvRx.mockStateHolder
        val frag = Frag()
        val viewMocks = MvRxViewMocks.getFrom(frag)
        val mockToUse = viewMocks.mocks.first { it.isDefaultInitialization }
        holder.setMock(frag, mockToUse)

        val mockedState = holder.getMockedState(
            view = frag,
            viewModelProperty = Frag::fragmentVm,
            existingViewModel = true,
            stateClass = TestState::class.java,
            forceMockExistingViewModel = false
        )

        assertEquals(mockToUse.states.single().state, mockedState)
    }

    @Test
    fun forceMockExistingViewModel() {
        val holder = MvRx.mockStateHolder
        val frag = Frag()

        val mockedState = holder.getMockedState(
            view = frag,
            viewModelProperty = Frag::fragmentVm,
            existingViewModel = true,
            stateClass = TestState::class.java,
            forceMockExistingViewModel = true
        )

        assertEquals(TestState(num = 3), mockedState)
    }

    @Test(expected = Throwable::class)
    fun forceMockExistingViewModelThrowsIfNoMocks() {
        val holder = MvRx.mockStateHolder
        val frag = FragWithNoMocks()

        holder.getMockedState(
            view = frag,
            viewModelProperty = FragWithNoMocks::fragmentVm,
            existingViewModel = true,
            stateClass = TestState::class.java,
            forceMockExistingViewModel = true
        )
    }


    class Frag : BaseMvRxFragment(), MockableMvRxView {
        val fragmentVm: FragmentVM by fragmentViewModel()

        override fun invalidate() {

        }

        override fun provideMocks() = mockSingleViewModel(
            Frag::fragmentVm,
            defaultState = TestState(num = 3),
            defaultArgs = null
        ) {

        }
    }

    class FragWithNoMocks : BaseMvRxFragment() {
        val fragmentVm: FragmentVM by fragmentViewModel()

        override fun invalidate() {

        }
    }

    data class TestState(val num: Int = 0) : MvRxState
    class FragmentVM(initialState: TestState) : BaseMvRxViewModel<TestState>(initialState)

}