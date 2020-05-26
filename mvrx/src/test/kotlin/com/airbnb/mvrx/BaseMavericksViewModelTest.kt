package com.airbnb.mvrx

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

data class BaseMavericksViewModelTestState(
    val asyncInt: Async<Int> = Uninitialized,
    val int: Int = 0
) : MvRxState

class BaseMavericksViewModelTestViewModel : BaseMavericksViewModel<BaseMavericksViewModelTestState>(BaseMavericksViewModelTestState()) {
    suspend fun runInViewModel(block: suspend BaseMavericksViewModelTestViewModel.() -> Unit) {
        block()
    }
}

@ExperimentalCoroutinesApi
class BaseMavericksViewModelTest : BaseTest() {

    private lateinit var viewModel: BaseMavericksViewModelTestViewModel

    @Before
    fun setup() {
        viewModel = BaseMavericksViewModelTestViewModel()
    }

    @Test
    fun testAsyncSuccess() = runInViewModelBlocking(
        BaseMavericksViewModelTestState(asyncInt = Uninitialized),
        BaseMavericksViewModelTestState(asyncInt = Loading<Int>()),
        BaseMavericksViewModelTestState(asyncInt = Success(5))
    ) {
        suspend {
            5
        }.execute { copy(asyncInt = it) }
    }

    @Test
    fun testAsyncSuccessWithRetainValue() = runInViewModelBlocking(
        BaseMavericksViewModelTestState(asyncInt = Uninitialized),
        BaseMavericksViewModelTestState(asyncInt = Loading<Int>()),
        BaseMavericksViewModelTestState(asyncInt = Success(5)),
        BaseMavericksViewModelTestState(asyncInt = Loading<Int>(value = 5)),
        BaseMavericksViewModelTestState(asyncInt = Success(7))
    ) {
        suspend {
            5
        }.execute(retainValue = BaseMavericksViewModelTestState::asyncInt) { copy(asyncInt = it) }
        suspend {
            7
        }.execute(retainValue = BaseMavericksViewModelTestState::asyncInt) { copy(asyncInt = it) }
    }

    @Test
    fun testAsyncFail() = runInViewModelBlocking(
        BaseMavericksViewModelTestState(asyncInt = Uninitialized),
        BaseMavericksViewModelTestState(asyncInt = Loading<Int>()),
        BaseMavericksViewModelTestState(asyncInt = Fail(exception))
    ) {
        suspend {
            throw exception
        }.execute(retainValue = BaseMavericksViewModelTestState::asyncInt) { copy(asyncInt = it) }
    }

    @Test
    fun testAsyncFailWithRetainValue() = runInViewModelBlocking(
        BaseMavericksViewModelTestState(asyncInt = Uninitialized),
        BaseMavericksViewModelTestState(asyncInt = Loading<Int>()),
        BaseMavericksViewModelTestState(asyncInt = Success(5)),
        BaseMavericksViewModelTestState(asyncInt = Loading<Int>(value = 5)),
        BaseMavericksViewModelTestState(asyncInt = Fail(exception, value = 5))
    ) {
        suspend {
            5
        }.execute(retainValue = BaseMavericksViewModelTestState::asyncInt) { copy(asyncInt = it) }
        suspend {
            throw exception
        }.execute(retainValue = BaseMavericksViewModelTestState::asyncInt) { copy(asyncInt = it) }
    }

    @Test
    fun testDeferredSuccess() = runInViewModelBlocking(
        BaseMavericksViewModelTestState(asyncInt = Uninitialized),
        BaseMavericksViewModelTestState(asyncInt = Loading<Int>()),
        BaseMavericksViewModelTestState(asyncInt = Success(5))
    ) {
        val deferedValue = CompletableDeferred<Int>()
        deferedValue.execute { copy(asyncInt = it) }
        delay(1000)
        deferedValue.complete(5)
    }

    @Test
    fun testDeferredFail() = runInViewModelBlocking(
        BaseMavericksViewModelTestState(asyncInt = Uninitialized),
        BaseMavericksViewModelTestState(asyncInt = Loading<Int>()),
        BaseMavericksViewModelTestState(asyncInt = Fail(exception))
    ) {
        val deferedValue = CompletableDeferred<Int>()
        deferedValue.execute { copy(asyncInt = it) }
        delay(1000)
        deferedValue.completeExceptionally(exception)
    }

    @Test
    fun testFlowExecute() = runInViewModelBlocking(
        BaseMavericksViewModelTestState(asyncInt = Uninitialized),
        BaseMavericksViewModelTestState(asyncInt = Loading<Int>()),
        BaseMavericksViewModelTestState(asyncInt = Success(1)),
        BaseMavericksViewModelTestState(asyncInt = Success(2))
    ) {
        flowOf(1, 2).execute { copy(asyncInt = it) }
    }

    @Test
    fun testFlowSetOnEach() = runInViewModelBlocking(
        BaseMavericksViewModelTestState(int = 0),
        BaseMavericksViewModelTestState(int = 1),
        BaseMavericksViewModelTestState(int = 2)
    ) {
        flowOf(1, 2).setOnEach { copy(int = it) }
    }

    private fun runInViewModelBlocking(vararg expectedState: BaseMavericksViewModelTestState, block: suspend BaseMavericksViewModelTestViewModel.() -> Unit) = runBlockingTest {
        val states = mutableListOf<BaseMavericksViewModelTestState>()
        viewModel.stateFlow.onEach { states += it }.launchIn(this)
        viewModel.runInViewModel(block)
        viewModel.onCleared()
        // We stringify the state list to make all exceptions equal each other. Without it, the stack traces cause tests to fail.
        assertEquals(expectedState.toList().toString(), states.toString())
    }

    companion object {
        private val exception = IllegalStateException("Fail!")
    }
}