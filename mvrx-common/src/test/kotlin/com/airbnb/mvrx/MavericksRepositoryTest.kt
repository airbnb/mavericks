package com.airbnb.mvrx

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.reflect.KProperty1

data class MavericksRepositoryTestState(
    val asyncInt: Async<Int> = Uninitialized,
    val int: Int = 0
) : MavericksState

class MavericksRepositoryTestRepository : BaseTestMavericksRepository<MavericksRepositoryTestState>(MavericksRepositoryTestState()) {
    suspend fun runInRepository(block: suspend MavericksRepositoryTestRepository.() -> Unit) {
        block()
    }

    fun setInt(int: Int) = setState { copy(int = int) }

    fun <T : Any?> (suspend () -> T).executePublic(
        dispatcher: CoroutineDispatcher? = null,
        retainValue: KProperty1<MavericksRepositoryTestState, Async<T>>? = null,
        reducer: MavericksRepositoryTestState.(Async<T>) -> MavericksRepositoryTestState
    ) {
        execute(dispatcher, retainValue, reducer)
    }

    fun <T> Flow<T>.executePublic(
        dispatcher: CoroutineDispatcher? = null,
        retainValue: KProperty1<MavericksRepositoryTestState, Async<T>>? = null,
        reducer: MavericksRepositoryTestState.(Async<T>) -> MavericksRepositoryTestState
    ) {
        execute(dispatcher, retainValue, reducer)
    }

    fun <T> Deferred<T>.executePublic(
        dispatcher: CoroutineDispatcher? = null,
        retainValue: KProperty1<MavericksRepositoryTestState, Async<T>>? = null,
        reducer: MavericksRepositoryTestState.(Async<T>) -> MavericksRepositoryTestState
    ) = suspend { await() }.execute(dispatcher, retainValue, reducer)

    fun <T> Flow<T>.setOnEachPublic(
        dispatcher: CoroutineDispatcher? = null,
        reducer: MavericksRepositoryTestState.(T) -> MavericksRepositoryTestState
    ) {
        setOnEach(dispatcher, reducer)
    }
}

class MavericksRepositoryTest : BaseTest() {

    private lateinit var repository: MavericksRepositoryTestRepository

    @Before
    fun setup() {
        repository = MavericksRepositoryTestRepository()
    }

    @Test
    fun testAsyncSuccess() = runInRepositoryBlocking(
        MavericksRepositoryTestState(asyncInt = Uninitialized),
        MavericksRepositoryTestState(asyncInt = Loading()),
        MavericksRepositoryTestState(asyncInt = Success(5))
    ) {
        suspend {
            5
        }.executePublic { copy(asyncInt = it) }
    }

    @Test
    fun testAsyncSuccessWithRetainValue() = runInRepositoryBlocking(
        MavericksRepositoryTestState(asyncInt = Uninitialized),
        MavericksRepositoryTestState(asyncInt = Loading()),
        MavericksRepositoryTestState(asyncInt = Success(5)),
        MavericksRepositoryTestState(asyncInt = Loading(value = 5)),
        MavericksRepositoryTestState(asyncInt = Success(7))
    ) {
        suspend {
            5
        }.executePublic(retainValue = MavericksRepositoryTestState::asyncInt) { copy(asyncInt = it) }
        suspend {
            7
        }.executePublic(retainValue = MavericksRepositoryTestState::asyncInt) { copy(asyncInt = it) }
    }

    @Test
    fun testAsyncFail() = runInRepositoryBlocking(
        MavericksRepositoryTestState(asyncInt = Uninitialized),
        MavericksRepositoryTestState(asyncInt = Loading()),
        MavericksRepositoryTestState(asyncInt = Fail(exception))
    ) {
        suspend {
            throw exception
        }.executePublic(retainValue = MavericksRepositoryTestState::asyncInt) { copy(asyncInt = it) }
    }

    @Test
    fun testAsyncFailWithRetainValue() = runInRepositoryBlocking(
        MavericksRepositoryTestState(asyncInt = Uninitialized),
        MavericksRepositoryTestState(asyncInt = Loading()),
        MavericksRepositoryTestState(asyncInt = Success(5)),
        MavericksRepositoryTestState(asyncInt = Loading(value = 5)),
        MavericksRepositoryTestState(asyncInt = Fail(exception, value = 5))
    ) {
        suspend {
            5
        }.executePublic(retainValue = MavericksRepositoryTestState::asyncInt) { copy(asyncInt = it) }
        suspend {
            throw exception
        }.executePublic(retainValue = MavericksRepositoryTestState::asyncInt) { copy(asyncInt = it) }
    }

    @Test
    fun testDeferredSuccess() = runInRepositoryBlocking(
        MavericksRepositoryTestState(asyncInt = Uninitialized),
        MavericksRepositoryTestState(asyncInt = Loading()),
        MavericksRepositoryTestState(asyncInt = Success(5))
    ) {
        val deferredValue = CompletableDeferred<Int>()
        deferredValue.executePublic { copy(asyncInt = it) }
        delay(1000)
        deferredValue.complete(5)
    }

    @Test
    fun testDeferredFail() = runInRepositoryBlocking(
        MavericksRepositoryTestState(asyncInt = Uninitialized),
        MavericksRepositoryTestState(asyncInt = Loading()),
        MavericksRepositoryTestState(asyncInt = Fail(exception))
    ) {
        val deferredValue = CompletableDeferred<Int>()
        deferredValue.executePublic { copy(asyncInt = it) }
        delay(1000)
        deferredValue.completeExceptionally(exception)
    }

    @Test
    fun testFlowExecute() = runInRepositoryBlocking(
        MavericksRepositoryTestState(asyncInt = Uninitialized),
        MavericksRepositoryTestState(asyncInt = Loading()),
        MavericksRepositoryTestState(asyncInt = Success(1)),
        MavericksRepositoryTestState(asyncInt = Success(2))
    ) {
        flowOf(1, 2).executePublic { copy(asyncInt = it) }
    }

    @Test
    fun testFlowExecuteWithRetainValue() = runInRepositoryBlocking(
        MavericksRepositoryTestState(asyncInt = Uninitialized),
        MavericksRepositoryTestState(asyncInt = Loading()),
        MavericksRepositoryTestState(asyncInt = Success(5)),
        MavericksRepositoryTestState(asyncInt = Fail(exception, value = 5))
    ) {
        flow {
            emit(5)
            throw exception
        }.executePublic(retainValue = MavericksRepositoryTestState::asyncInt) { copy(asyncInt = it) }
    }

    @Test
    fun testFlowSetOnEach() = runInRepositoryBlocking(
        MavericksRepositoryTestState(int = 0),
        MavericksRepositoryTestState(int = 1),
        MavericksRepositoryTestState(int = 2)
    ) {
        flowOf(1, 2).setOnEachPublic { copy(int = it) }
    }

    @Test
    fun testAwaitState() = runInRepositoryBlocking(
        MavericksRepositoryTestState(int = 0),
        MavericksRepositoryTestState(int = 1),
    ) {
        setInt(1)
        val state = awaitState()
        assertEquals(1, state.int)
    }

    private fun runInRepositoryBlocking(
        vararg expectedState: MavericksRepositoryTestState,
        block: suspend MavericksRepositoryTestRepository.() -> Unit
    ) = runTest(UnconfinedTestDispatcher()) {
        val states = mutableListOf<MavericksRepositoryTestState>()
        val consumerJob = repository.stateFlow.onEach { states += it }.launchIn(this)
        repository.runInRepository(block)
        repository.tearDown()
        consumerJob.cancel()
        // We stringify the state list to make all exceptions equal each other. Without it, the stack traces cause tests to fail.
        assertEquals(expectedState.toList().toString(), states.toString())
    }

    companion object {
        private val exception = IllegalStateException("Fail!")
    }
}
