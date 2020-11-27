package com.airbnb.mvrx.mocking

import android.os.Parcelable
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.fragmentViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class MockStateHolderTest : BaseTest() {

    @Before
    fun setup() {
        MockableMavericks.mockConfigFactory.mockBehavior = MockBehavior(
            initialStateMocking = MockBehavior.InitialStateMocking.Full,
            stateStoreBehavior = MockBehavior.StateStoreBehavior.Scriptable
        )
    }

    @Test
    fun getDefaultState() {
        val holder = MockableMavericks.mockStateHolder
        val frag = Frag()
        val viewMocks = MavericksViewMocks.getFrom(frag)
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
    fun getDefaultStateReflectiveProvider() {
        MavericksViewMocks.mockProvider = object : MavericksViewMocks.ViewMocksProvider {
            override fun mavericksViewMocks(view: MockableMavericksView): MavericksViewMocks<out MockableMavericksView, out Parcelable> {
                return when (view) {
                    is Frag -> {
                        view.call("provideMocks")
                    }
                    else -> MavericksViewMocks.DefaultViewMocksProvider.mavericksViewMocks(view)
                }
            }
        }

        val holder = MockableMavericks.mockStateHolder
        val frag = Frag()
        val viewMocks = MavericksViewMocks.getFrom(frag)
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
    fun getDefaultStateOverridenProvider() {
        MavericksViewMocks.mockProvider = object : MavericksViewMocks.ViewMocksProvider {
            override fun mavericksViewMocks(view: MockableMavericksView): MavericksViewMocks<out MockableMavericksView, out Parcelable> {
                return when (view) {
                    is Frag -> {
                        val viewModelReference = Frag::fragmentVm
                        val defaultState = TestState(num = 4)
                        view.mockSingleViewModel(
                            viewModelReference = viewModelReference,
                            defaultState = defaultState,
                            defaultArgs = null
                        ) {

                        }
                    }
                    else -> MavericksViewMocks.DefaultViewMocksProvider.mavericksViewMocks(view)
                }
            }
        }

        val holder = MockableMavericks.mockStateHolder
        val frag = Frag()
        val viewMocks = MavericksViewMocks.getFrom(frag)
        val mockToUse = viewMocks.mocks.first { it.isDefaultState }
        holder.setMock(frag, mockToUse)

        assertEquals(mockToUse.states.single().state, TestState(num = 4))
    }

    @Test
    fun clearMock() {
        val holder = MockableMavericks.mockStateHolder
        val frag = Frag()
        val viewMocks = MavericksViewMocks.getFrom(frag)
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
        val holder = MockableMavericks.mockStateHolder
        val frag = Frag()
        val viewMocks = MavericksViewMocks.getFrom(frag)
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
        val holder = MockableMavericks.mockStateHolder
        val frag = Frag()
        val viewMocks = MavericksViewMocks.getFrom(frag)
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
        val holder = MockableMavericks.mockStateHolder
        val frag = Frag()
        val viewMocks = MavericksViewMocks.getFrom(frag)
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
        val holder = MockableMavericks.mockStateHolder
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
        val holder = MockableMavericks.mockStateHolder
        val frag = FragWithNoMocks()

        holder.getMockedState(
            view = frag,
            viewModelProperty = FragWithNoMocks::fragmentVm,
            existingViewModel = true,
            stateClass = TestState::class.java,
            forceMockExistingViewModel = true
        )
    }

    @Test
    fun forceMockExistingViewModelDoesNotThrowIfFragmentNoMocksAndMockProviderReturnsMocks() {
        MavericksViewMocks.mockProvider = object : MavericksViewMocks.ViewMocksProvider {
            override fun mavericksViewMocks(view: MockableMavericksView): MavericksViewMocks<out MockableMavericksView, out Parcelable> {
                return when (view) {
                    is FragWithNoMocks -> {
                        view.mockSingleViewModel(
                            viewModelReference = FragWithNoMocks::fragmentVm,
                            defaultState = TestState(num = 4),
                            defaultArgs = null
                        ) {

                        }
                    }
                    else -> MavericksViewMocks.DefaultViewMocksProvider.mavericksViewMocks(view)
                }
            }
        }

        val holder = MockableMavericks.mockStateHolder
        val frag = FragWithNoMocks()

        val mockedState = holder.getMockedState(
            view = frag,
            viewModelProperty = FragWithNoMocks::fragmentVm,
            existingViewModel = true,
            stateClass = TestState::class.java,
            forceMockExistingViewModel = true
        )

        assertEquals(TestState(num = 4), mockedState)
    }

    class Frag : Fragment(), MockableMavericksView {
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

    class FragWithNoMocks : Fragment(), MockableMavericksView {
        val fragmentVm: FragmentVM by fragmentViewModel()

        override fun invalidate() {
        }
    }

    data class TestState(val num: Int = 0) : MavericksState
    class FragmentVM(initialState: TestState) : MavericksViewModel<TestState>(initialState)
}