package com.airbnb.mvrx.mocking

import com.airbnb.mvrx.MavericksViewModelConfig
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.mocking.MockBehavior.InitialStateMocking
import com.airbnb.mvrx.mocking.MockBehavior.StateStoreBehavior
import com.airbnb.mvrx.test.MvRxTestRule
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.ClassRule
import org.junit.Test

class MockableMavericksStateStoreTest : BaseTest() {

    companion object {
        @JvmField
        @ClassRule
        val mvrxTestRule = MvRxTestRule()
    }

    @Test
    fun addOnDisposedCallback() {
        val disposedStores = mutableSetOf<MockableMavericksStateStore<*>>()
        val store = createStore().apply {
            addOnCancelListener { disposedStores.add(it) }
        }

        assertTrue(disposedStores.isEmpty())

        store.coroutineScope.coroutineContext[Job]!!.invokeOnCompletion {
            assertEquals(setOf(store), disposedStores)
        }

        store.coroutineScope.cancel()
    }

    @Test
    fun onDisposedCalledImmediatelyIfAlreadyDisposed() {
        val disposedStores = mutableSetOf<MockableMavericksStateStore<*>>()
        val store = createStore()
        store.coroutineScope.cancel()

        store.addOnCancelListener { disposedStores.add(it) }
        assertEquals(setOf(store), disposedStores)
    }

    @Test
    fun setStateNoopWhenScriptable() {
        val store = createStore()
        store.set { TestState(5) }
        store.get { state ->
            assertEquals(0, state.num)
        }
    }

    @Test
    fun setStateCallbackWhenScriptable() {
        val store = createStore()

        var previousState: TestState? = null
        var newState: TestState? = null
        store.addOnStateSetListener { prev, new ->
            previousState = prev
            newState = new
        }

        store.set { TestState(5) }

        assertEquals(TestState(), previousState)
        assertEquals(TestState(5), newState)
    }

    @Test
    fun removeSetStateCallback() {
        val store = createStore()

        var previousState: TestState? = null
        var newState: TestState? = null
        val stateCallback = { prev: TestState, new: TestState ->
            previousState = prev
            newState = new
        }
        store.addOnStateSetListener(stateCallback)
        store.removeOnStateSetListener(stateCallback)

        store.set { TestState(5) }

        assertNull(previousState)
        assertNull(newState)
    }

    @Test
    fun nextStateWhenScriptable() {
        val store = createStore()

        store.next(TestState(5))

        store.get { state ->
            assertEquals(5, state.num)
        }
    }

    @Test
    fun nextTriggersObservable() = runBlocking {
        val store = createStore()

        store.next(TestState(5))

        val flowValue = store.flow.firstOrNull()
        assertEquals(TestState(5), flowValue)
    }

    @Test(expected = IllegalStateException::class)
    fun nextStateWhenNormalBehaviorFails() {
        val store = createStore(StateStoreBehavior.Normal)
        store.next(TestState(5))
    }

    @Test(expected = IllegalStateException::class)
    fun nextStateWhenSynchronousBehaviorFails() {
        val store = createStore(StateStoreBehavior.Synchronous)
        store.next(TestState(5))
    }

    @Test
    fun setAndGetStateSynchronously() {
        val store = createStore(StateStoreBehavior.Synchronous)
        store.set { TestState(5) }
        store.get { state ->
            assertEquals(5, state.num)
        }
    }

    @Test
    fun setAndGetStateNormally() {
        // Normal behavior would be async, but the test environment forces it synchronously
        val store = createStore(StateStoreBehavior.Normal)
        store.set { TestState(5) }
        store.get { state ->
            assertEquals(5, state.num)
        }
    }

    @Test
    fun switchingFromScriptableToSynchronous() {
        val store = createStore()
        store.next(TestState(5))

        store.mockBehavior =
            store.mockBehavior.copy(stateStoreBehavior = StateStoreBehavior.Synchronous)

        // State forced by "next" is inherited when the state stores are switched
        store.get { state -> assertEquals(5, state.num) }

        // New state store behavior takes effect, and we can set states normally
        store.set { TestState(6) }
        store.get { state -> assertEquals(6, state.num) }
    }

    @Test
    fun switchingFromSynchronousToScriptable() {
        val store = createStore(StateStoreBehavior.Synchronous)
        store.set { TestState(5) }

        store.mockBehavior =
            store.mockBehavior.copy(stateStoreBehavior = StateStoreBehavior.Scriptable)

        // State forced by "set" is inherited when the state stores are switched
        store.get { state ->
            assertEquals(5, state.num)
        }

        // New state store behavior takes effect, and we can script state
        store.next(TestState(6))
        store.get { state -> assertEquals(6, state.num) }
    }

    private fun createStore(
        storeBehavior: StateStoreBehavior = StateStoreBehavior.Scriptable
    ): MockableMavericksStateStore<TestState> {
        return MockableMavericksStateStore(
            TestState(),
            MockBehavior(
                initialStateMocking = InitialStateMocking.None,
                blockExecutions = MavericksViewModelConfig.BlockExecutions.No,
                stateStoreBehavior = storeBehavior
            ),
            coroutineScope = testCoroutineScope()
        )
    }

    private data class TestState(val num: Int = 0) : MavericksState
}