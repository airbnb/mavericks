package com.airbnb.mvrx.mocking

import android.os.Parcelable
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.fragmentViewModel
import kotlinx.android.parcel.Parcelize
import org.hamcrest.core.IsEqual
import org.hamcrest.core.StringContains
import org.hamcrest.core.StringEndsWith
import org.hamcrest.core.StringStartsWith
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import kotlin.reflect.KClass

class MockBuilderTest : BaseTest() {

    @Suppress("DEPRECATION")
    @Rule
    @JvmField
    val expectedException: ExpectedException = ExpectedException.none()

    private fun <T : Throwable> ExpectedException.expect(
        clazz: KClass<T>,
        errorMessage: (() -> String)? = null
    ) {
        expect(clazz.java)
        errorMessage?.invoke()?.let { expectMessage(it) }
    }

    private lateinit var fragment: TestFragment

    @Before
    fun setup() {
        fragment = TestFragment()
    }

    private fun <Args : Parcelable> mockNoViewModels(
        defaultArgs: Args?,
        block: MockBuilder<TestFragment, Args>.() -> Unit
    ): MockBuilder<TestFragment, Args> = fragment.run {
        MavericksViewMocks.allowCreationForTesting {
            mockNoViewModels(defaultArgs, block)
        }
    }

    private fun <Args : Parcelable> mockSingleViewModel(
        defaultArgs: Args?,
        block: SingleViewModelMockBuilder<TestFragment, Args, State>.() -> Unit
    ): MockBuilder<TestFragment, Args> = fragment.run {
        MavericksViewMocks.allowCreationForTesting {
            mockSingleViewModel(TestFragment::viewModel1, baseState, defaultArgs, block)
        }
    }

    private fun <Args : Parcelable> mockTwoViewModels(
        defaultArgs: Args? = null,
        block: TwoViewModelMockBuilder<TestFragment, TestViewModel, State, TestViewModel, State, Args>.() -> Unit
    ): MockBuilder<TestFragment, Args> = fragment.run {
        MavericksViewMocks.allowCreationForTesting {
            mockTwoViewModels(
                TestFragment::viewModel1,
                baseState,
                TestFragment::viewModel2,
                baseState,
                defaultArgs,
                block
            )
        }
    }

    private fun <Args : Parcelable> mockThreeViewModels(
        defaultArgs: Args? = null,
        block: ThreeViewModelMockBuilder<TestFragment, TestViewModel, State, TestViewModel, State, TestViewModel, State, Args>.() -> Unit
    ): MockBuilder<TestFragment, Args> = fragment.run {
        MavericksViewMocks.allowCreationForTesting {
            mockThreeViewModels(
                TestFragment::viewModel1,
                baseState,
                TestFragment::viewModel2,
                baseState,
                TestFragment::viewModel3,
                baseState,
                defaultArgs,
                block
            )
        }
    }

    @Test
    fun mockNoViewModelWithArgs() {
        val mocks = mockNoViewModels(Args(3)) {

            args("other args") {
                Args(5)
            }
        }

        val initializationMock = mocks.mocks[0]
        assertTrue(initializationMock.states.isEmpty())
        assertTrue(initializationMock.forInitialization)
        assertEquals("Default initialization", initializationMock.name)
        assertEquals(3, initializationMock.args?.num)

        val argMock = mocks.mocks[1]

        assertTrue(argMock.states.isEmpty())
        assertTrue(argMock.forInitialization)
        assertEquals("other args", argMock.name)
        assertEquals(5, argMock.args?.num)
    }

    @Test
    fun mockNoViewModelWithNoArgs() {
        val mocks = mockNoViewModels(null) {}

        val fragmentMock = mocks.mocks.single()
        assertTrue(fragmentMock.states.isEmpty())
        assertEquals("Default initialization", fragmentMock.name)
        assertNull(fragmentMock.args)
        assertTrue(fragmentMock.forInitialization)
    }

    @Test
    fun testSingleViewModelState() {
        val mocks = mockSingleViewModel(null) {
            state("custom state") {
                set { ::bookingDetails { ::disclaimerInfo { ::text } } }.with { "hello" }
                    .set { ::bookingDetails { ::num } }.with { 4 }
            }
        }

        assertEquals(4, mocks.mocks.size)

        mocks.mocks[0].let { initializationMock ->
            assertNull(initializationMock.args)
            assertTrue(initializationMock.forInitialization)
            assertEquals("Default initialization", initializationMock.name)

            val initializationState = initializationMock.states.single().state
            assertEquals(baseState, initializationState)
        }

        mocks.mocks[1].let { defaultStateMock ->
            assertNull(defaultStateMock.args)
            assertFalse(defaultStateMock.forInitialization)
            assertEquals("Default state", defaultStateMock.name)

            val defaultState = defaultStateMock.states.single().state
            assertEquals(baseState, defaultState)
        }

        mocks.mocks[3].let { customStateMock ->
            assertNull(customStateMock.args)
            assertFalse(customStateMock.forInitialization)
            assertEquals("custom state", customStateMock.name)

            val mockDetails = customStateMock.states.single()
            assertEquals(TestFragment::viewModel1, mockDetails.viewModelProperty)

            val state = mockDetails.state as State
            assertEquals("hello", state.bookingDetails.disclaimerInfo?.text)
            assertEquals(4, state.bookingDetails.num)
        }
    }

    @Test
    fun testTwoViewModels() {

        val defaultArgs = Args(1)
        val mocks = mockTwoViewModels(defaultArgs) {

            state("state 1") {

                viewModel1 {
                    set { ::bookingDetails { ::num } }.with { 0 }
                }

                viewModel2 {
                    set { ::bookingDetails { ::num } }.with { 1 }
                }
            }

            state("state 2") {

                viewModel1 {
                    set { ::bookingDetails { ::num } }.with { 54 }
                }
            }
        }

        assertEquals(5, mocks.mocks.size)

        mocks.mocks[0].let { initializationMock ->
            assertEquals(defaultArgs, initializationMock.args)
            assertTrue(initializationMock.forInitialization)
            assertEquals("Default initialization", initializationMock.name)

            assertEquals(listOf(baseState, baseState), initializationMock.states.map { it.state })
        }

        mocks.mocks[1].let { defaultStateMock ->
            assertEquals(defaultArgs, defaultStateMock.args)
            assertFalse(defaultStateMock.forInitialization)
            assertEquals("Default state", defaultStateMock.name)

            assertEquals(listOf(baseState, baseState), defaultStateMock.states.map { it.state })
        }

        mocks.mocks[2].let { restoredStateMock ->
            assertEquals(defaultArgs, restoredStateMock.args)
            assertFalse(restoredStateMock.forInitialization)
            assertTrue(restoredStateMock.isForProcessRecreation)
            assertEquals("Default state after process recreation", restoredStateMock.name)

            assertEquals(listOf(baseState, baseState), restoredStateMock.states.map { it.state })
        }

        mocks.mocks[3].let { state1Mock ->

            assertEquals("state 1", state1Mock.name)
            assertFalse(state1Mock.forInitialization)
            assertEquals(defaultArgs, state1Mock.args)

            val viewModelList = state1Mock.states
            assertEquals(2, viewModelList.size)

            val firstViewModelMock = viewModelList[0]
            val secondViewModelMock = viewModelList[1]

            assertEquals(TestFragment::viewModel1, firstViewModelMock.viewModelProperty)
            assertEquals(TestFragment::viewModel2, secondViewModelMock.viewModelProperty)

            assertEquals(0, firstViewModelMock.state.cast<State>().bookingDetails.num)
            assertEquals(1, secondViewModelMock.state.cast<State>().bookingDetails.num)
        }

        mocks.mocks[4].let { state1Mock ->

            assertEquals("state 2", state1Mock.name)

            val viewModelList = state1Mock.states
            assertEquals(2, viewModelList.size)

            val firstViewModelMock = viewModelList[0]
            val secondViewModelMock = viewModelList[1]

            assertEquals(TestFragment::viewModel1, firstViewModelMock.viewModelProperty)
            assertEquals(TestFragment::viewModel2, secondViewModelMock.viewModelProperty)

            assertEquals(54, firstViewModelMock.state.cast<State>().bookingDetails.num)
            assertEquals(
                baseState.bookingDetails.num,
                secondViewModelMock.state.cast<State>().bookingDetails.num
            )
        }
    }

    @Test
    fun testThreeViewModels() {

        val mocks = mockThreeViewModels(Args(1)) {

            state("state") {

                viewModel1 {
                    set { ::bookingDetails { ::num } }.with { 0 }
                }

                viewModel2 {
                    set { ::bookingDetails { ::num } }.with { 1 }
                }

                viewModel3 {
                    set { ::bookingDetails { ::num } }.with { 2 }
                }
            }
        }

        val fragmentMock =
            mocks.mocks[3] // The first three mocks are the default args and state mocks
        assertEquals("state", fragmentMock.name)
        assertFalse(fragmentMock.forInitialization)
        assertEquals(1, fragmentMock.args?.num)

        val viewModelList = fragmentMock.states
        assertEquals(3, viewModelList.size)

        val firstViewModelMock = viewModelList[0]
        val secondViewModelMock = viewModelList[1]
        val thirdViewModelMock = viewModelList[2]

        assertEquals(TestFragment::viewModel1, firstViewModelMock.viewModelProperty)
        assertEquals(TestFragment::viewModel2, secondViewModelMock.viewModelProperty)
        assertEquals(TestFragment::viewModel3, thirdViewModelMock.viewModelProperty)

        assertEquals(0, firstViewModelMock.state.cast<State>().bookingDetails.num)
        assertEquals(1, secondViewModelMock.state.cast<State>().bookingDetails.num)
        assertEquals(2, thirdViewModelMock.state.cast<State>().bookingDetails.num)
    }

    @Test
    fun testThreeViewModelsWithDefaultStates() {

        val mocks = mockThreeViewModels(Args(1)) {

            state("state") {
            }
        }

        val fragmentMock = mocks.mocks[3]
        assertEquals("state", fragmentMock.name)
        assertFalse(fragmentMock.forInitialization)
        assertEquals(1, fragmentMock.args?.num)

        val viewModelList = fragmentMock.states
        assertEquals(3, viewModelList.size)

        val firstViewModelMock = viewModelList[0]
        val secondViewModelMock = viewModelList[1]
        val thirdViewModelMock = viewModelList[2]

        assertEquals(TestFragment::viewModel1, firstViewModelMock.viewModelProperty)
        assertEquals(TestFragment::viewModel2, secondViewModelMock.viewModelProperty)
        assertEquals(TestFragment::viewModel3, thirdViewModelMock.viewModelProperty)

        assertEquals(
            baseState.bookingDetails.num,
            firstViewModelMock.state.cast<State>().bookingDetails.num
        )
        assertEquals(
            baseState.bookingDetails.num,
            secondViewModelMock.state.cast<State>().bookingDetails.num
        )
        assertEquals(
            baseState.bookingDetails.num,
            thirdViewModelMock.state.cast<State>().bookingDetails.num
        )
    }

    @Test
    fun mockAsyncSuccessProperty() {
        val mocks = mockSingleViewModel(null) {
            state("my state") {
                set { ::asyncBookingDetails { success { ::disclaimerInfo { ::text } } } }.with { "hello" }
                    .set { ::asyncDisclaimer { success { ::text } } }.with { "hello again" }
                    .set { ::asyncInt }.with { Success(4) }
            }
        }

        val fragmentMock = mocks.mocks[3]
        val mockDetails = fragmentMock.states.single()

        val state = mockDetails.state as State
        assertEquals("hello", state.asyncBookingDetails()?.disclaimerInfo?.text)
        assertEquals("hello again", state.asyncDisclaimer()?.text)
        assertEquals(4, state.asyncInt())
    }

    @Test
    fun mockAsyncSuccessFailsWhenNotSuccess() {
        expectedException.expect<IllegalStateException>(msgContains = "Async value is not in the success state, it is `Uninitialized`")

        // Trying to set a value on the async property when it is uninitialized should give a clear error message
        val state = baseState.copy(asyncBookingDetails = Uninitialized)

        mockSingleViewModel(null) {
            state("my state") {
                state.set { ::asyncBookingDetails { success { ::disclaimerInfo { ::text } } } }
                    .with { "hello" }
            }
        }
    }

    @Test
    fun mockSettingPropertyFailsWhenNull() {
        expectedException.expect(IllegalArgumentException::class) {
            "The value for 'disclaimerInfo' is null, properties on it can't be changed."
        }

        // Trying to set a value on a null property should give a clear warning
        val state = baseState.copy(bookingDetails = BookingDetails(disclaimerInfo = null))

        mockSingleViewModel(null) {
            state("my state") {
                state.set { ::bookingDetails { ::disclaimerInfo { ::text } } }.with { "hello" }
            }
        }
    }

    class TestFragment : Fragment(), MockableMavericksView {
        val viewModel1: TestViewModel by fragmentViewModel()
        val viewModel2: TestViewModel by fragmentViewModel()
        val viewModel3: TestViewModel by fragmentViewModel()

        override fun invalidate() {
        }
    }

    class TestViewModel : MavericksViewModel<State>(baseState)

    @Parcelize
    class Args(val num: Int) : Parcelable

    data class State(
        val bookingDetails: BookingDetails,
        val asyncBookingDetails: Async<BookingDetails>,
        val asyncDisclaimer: Async<DisclaimerInfo>,
        val asyncInt: Async<Int>
    ) : MavericksState

    data class BookingDetails(val num: Int = 7, val disclaimerInfo: DisclaimerInfo?)
    data class DisclaimerInfo(val text: String)

    companion object {
        val baseState = State(
            bookingDetails = BookingDetails(disclaimerInfo = DisclaimerInfo("")),
            asyncBookingDetails = Success(BookingDetails(disclaimerInfo = DisclaimerInfo(""))),
            asyncDisclaimer = Success(DisclaimerInfo("")),
            asyncInt = Success(1)
        )
    }
}

@Suppress("UNCHECKED_CAST")
private fun <T> Any.cast(): T = this as T

/** Simplifies specifying an expected exception. */
inline fun <reified T : Throwable> ExpectedException.expect(
    msgStartsWith: String? = null,
    msgContains: String? = null,
    msgEndsWith: String? = null,
    exactMsg: String? = null
) {
    expect(T::class.java)

    msgContains?.let { expectMessage(StringContains(it)) }
    msgStartsWith?.let { expectMessage(StringStartsWith(it)) }
    msgEndsWith?.let { expectMessage(StringEndsWith(it)) }
    exactMsg?.let { expectMessage(IsEqual(it)) }
}
