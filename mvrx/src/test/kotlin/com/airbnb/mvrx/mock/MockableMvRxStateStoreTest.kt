package com.airbnb.mvrx.mock

import com.airbnb.mvrx.BaseTest
import com.airbnb.mvrx.MvRxViewModelConfig
import com.airbnb.mvrx.mock.MockBehavior.StateStoreBehavior
import org.junit.Assert.*
import org.junit.Test

class MockableMvRxStateStoreTest : BaseTest() {

    @Test
    fun isDisposed() {
        val store = createStore()
        store.dispose()
        assertTrue(store.isDisposed)
    }

    @Test
    fun addOnDisposedCallback() {
        val disposedStores = mutableSetOf<MockableMvRxStateStore<*>>()
        val store = createStore().apply {
            addOnDisposeListener { disposedStores.add(it) }
        }

        assertTrue(disposedStores.isEmpty())

        store.dispose()
        assertEquals(setOf(store), disposedStores)
    }

    @Test
    fun onDisposedCalledImmediatelyIfAlreadyDisposed() {
        val disposedStores = mutableSetOf<MockableMvRxStateStore<*>>()
        val store = createStore()
        store.dispose()

        store.addOnDisposeListener { disposedStores.add(it) }
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
    fun nextTriggersObservable() {
        val store = createStore()

        store.next(TestState(5))

        val testObserver = store.observable.firstOrError().test()
        testObserver.assertValue(TestState(5))
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
    ): MockableMvRxStateStore<TestState> {
        return MockableMvRxStateStore(
            TestState(),
            MockBehavior(
                initialState = MockBehavior.InitialState.None,
                blockExecutions = MvRxViewModelConfig.BlockExecutions.No,
                stateStoreBehavior = storeBehavior
            )
        )
    }

    private data class TestState(val num: Int = 0)
}