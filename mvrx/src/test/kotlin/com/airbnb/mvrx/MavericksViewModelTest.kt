package com.airbnb.mvrx

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.reflect.KProperty1

data class BaseMavericksViewModelTestState(
    val asyncInt: Async<Int> = Uninitialized,
    val int: Int = 0
) : MavericksState

class MavericksViewModelTestViewModel : MavericksViewModel<BaseMavericksViewModelTestState>(BaseMavericksViewModelTestState()) {
    suspend fun runInViewModel(block: suspend MavericksViewModelTestViewModel.() -> Unit) {
        block()
    }

    fun setInt(int: Int) = setState { copy(int = int) }

    fun <T : Any?> (suspend () -> T).executePublic (
        dispatcher: CoroutineDispatcher? = null,
        retainValue: KProperty1<BaseMavericksViewModelTestState, Async<T>>? = null,
        reducer: BaseMavericksViewModelTestState.(Async<T>) -> BaseMavericksViewModelTestState
    ) {
        execute(dispatcher, retainValue, reducer)
    }

    fun <T> Flow<T>.executePublic(
        dispatcher: CoroutineDispatcher? = null,
        retainValue: KProperty1<BaseMavericksViewModelTestState, Async<T>>? = null,
        reducer: BaseMavericksViewModelTestState.(Async<T>) -> BaseMavericksViewModelTestState
    ) {
        execute(dispatcher, retainValue, reducer)
    }

    fun <T> Deferred<T>.executePublic(
        dispatcher: CoroutineDispatcher? = null,
        retainValue: KProperty1<BaseMavericksViewModelTestState, Async<T>>? = null,
        reducer: BaseMavericksViewModelTestState.(Async<T>) -> BaseMavericksViewModelTestState
    ) = suspend { await() }.execute(dispatcher, retainValue, reducer)

    fun <T> Flow<T>.setOnEachPublic(
        dispatcher: CoroutineDispatcher? = null,
        reducer: BaseMavericksViewModelTestState.(T) -> BaseMavericksViewModelTestState
    ) {
        setOnEach(dispatcher, reducer)
    }
}

@ExperimentalCoroutinesApi
class MavericksViewModelTest : BaseTest() {

    private lateinit var viewModel: MavericksViewModelTestViewModel

    @Before
    fun setup() {
        viewModel = MavericksViewModelTestViewModel()
    }

    @Test
    fun testAsyncSuccess() = runInViewModelBlocking(
        BaseMavericksViewModelTestState(asyncInt = Uninitialized),
        BaseMavericksViewModelTestState(asyncInt = Loading()),
        BaseMavericksViewModelTestState(asyncInt = Success(5))
    ) {
        suspend {
            5
        }.executePublic { copy(asyncInt = it) }
    }

    @Test
    fun testAsyncSuccessWithRetainValue() = runInViewModelBlocking(
        BaseMavericksViewModelTestState(asyncInt = Uninitialized),
        BaseMavericksViewModelTestState(asyncInt = Loading()),
        BaseMavericksViewModelTestState(asyncInt = Success(5)),
        BaseMavericksViewModelTestState(asyncInt = Loading(value = 5)),
        BaseMavericksViewModelTestState(asyncInt = Success(7))
    ) {
        suspend {
            5
        }.executePublic(retainValue = BaseMavericksViewModelTestState::asyncInt) { copy(asyncInt = it) }
        suspend {
            7
        }.executePublic(retainValue = BaseMavericksViewModelTestState::asyncInt) { copy(asyncInt = it) }
    }

    @Test
    fun testAsyncFail() = runInViewModelBlocking(
        BaseMavericksViewModelTestState(asyncInt = Uninitialized),
        BaseMavericksViewModelTestState(asyncInt = Loading()),
        BaseMavericksViewModelTestState(asyncInt = Fail(exception))
    ) {
        suspend {
            throw exception
        }.executePublic(retainValue = BaseMavericksViewModelTestState::asyncInt) { copy(asyncInt = it) }
    }

    @Test
    fun testAsyncFailWithRetainValue() = runInViewModelBlocking(
        BaseMavericksViewModelTestState(asyncInt = Uninitialized),
        BaseMavericksViewModelTestState(asyncInt = Loading()),
        BaseMavericksViewModelTestState(asyncInt = Success(5)),
        BaseMavericksViewModelTestState(asyncInt = Loading(value = 5)),
        BaseMavericksViewModelTestState(asyncInt = Fail(exception, value = 5))
    ) {
        suspend {
            5
        }.executePublic(retainValue = BaseMavericksViewModelTestState::asyncInt) { copy(asyncInt = it) }
        suspend {
            throw exception
        }.executePublic(retainValue = BaseMavericksViewModelTestState::asyncInt) { copy(asyncInt = it) }
    }

    @Test
    fun testDeferredSuccess() = runInViewModelBlocking(
        BaseMavericksViewModelTestState(asyncInt = Uninitialized),
        BaseMavericksViewModelTestState(asyncInt = Loading()),
        BaseMavericksViewModelTestState(asyncInt = Success(5))
    ) {
        val deferredValue = CompletableDeferred<Int>()
        deferredValue.executePublic { copy(asyncInt = it) }
        delay(1000)
        deferredValue.complete(5)
    }

    @Test
    fun testDeferredFail() = runInViewModelBlocking(
        BaseMavericksViewModelTestState(asyncInt = Uninitialized),
        BaseMavericksViewModelTestState(asyncInt = Loading()),
        BaseMavericksViewModelTestState(asyncInt = Fail(exception))
    ) {
        val deferredValue = CompletableDeferred<Int>()
        deferredValue.executePublic { copy(asyncInt = it) }
        delay(1000)
        deferredValue.completeExceptionally(exception)
    }

    @Test
    fun testFlowExecute() = runInViewModelBlocking(
        BaseMavericksViewModelTestState(asyncInt = Uninitialized),
        BaseMavericksViewModelTestState(asyncInt = Loading()),
        BaseMavericksViewModelTestState(asyncInt = Success(1)),
        BaseMavericksViewModelTestState(asyncInt = Success(2))
    ) {
        flowOf(1, 2).executePublic { copy(asyncInt = it) }
    }

    @Test
    fun testFlowExecuteWithRetainValue() = runInViewModelBlocking(
        BaseMavericksViewModelTestState(asyncInt = Uninitialized),
        BaseMavericksViewModelTestState(asyncInt = Loading()),
        BaseMavericksViewModelTestState(asyncInt = Success(5)),
        BaseMavericksViewModelTestState(asyncInt = Fail(exception, value = 5))
    ) {
        flow {
            emit(5)
            throw exception
        }.executePublic(retainValue = BaseMavericksViewModelTestState::asyncInt) { copy(asyncInt = it) }
    }

    @Test
    fun testFlowSetOnEach() = runInViewModelBlocking(
        BaseMavericksViewModelTestState(int = 0),
        BaseMavericksViewModelTestState(int = 1),
        BaseMavericksViewModelTestState(int = 2)
    ) {
        flowOf(1, 2).setOnEachPublic { copy(int = it) }
    }

    @Test
    fun testAwaitState() = runInViewModelBlocking(
        BaseMavericksViewModelTestState(int = 0),
        BaseMavericksViewModelTestState(int = 1),
        ) {
        setInt(1)
        val state = awaitState()
        assertEquals(1, state.int)
    }

    private fun runInViewModelBlocking(
        vararg expectedState: BaseMavericksViewModelTestState,
        block: suspend MavericksViewModelTestViewModel.() -> Unit
    ) = runBlockingTest {
        val states = mutableListOf<BaseMavericksViewModelTestState>()
        val consumerJob = viewModel.stateFlow.onEach { states += it }.launchIn(this)
        viewModel.runInViewModel(block)
        viewModel.onCleared()
        consumerJob.cancel()
        // We stringify the state list to make all exceptions equal each other. Without it, the stack traces cause tests to fail.
        assertEquals(expectedState.toList().toString(), states.toString())
    }

    companion object {
        private val exception = IllegalStateException("Fail!")
    }
}