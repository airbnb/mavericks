package com.airbnb.mvrx

import android.arch.lifecycle.Lifecycle
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

data class MvRxStateStoreTestState(val count: Int = 1, val list: List<Int> = emptyList())

class MvRxStateStoreTest : MvRxBaseTest() {

    private lateinit var store: MvRxStateStore<MvRxStateStoreTestState>
    private lateinit var lifecycleOwner: TestLifecycleOwner
    private var callCount = 0
    private var lastAssertedCallCount = 0

    @Before
    fun setup() {
        store = MvRxStateStore(MvRxStateStoreTestState())
        callCount = 0
        store.subscribe { callCount++ }
        lifecycleOwner = TestLifecycleOwner()
    }

    @Test
    fun testSubscribeRunsSynchronouslyForTests() {
        assertSubscriberCalled()
    }

    @Test
    fun testGetRunsSynchronouslyForTests() {
        var callCount = 0
        store.get { callCount++ }
        assertEquals(1, callCount)
    }

    @Test
    fun testInitialState() {
        store.get { assertEquals(1, it.count) }
    }

    @Test
    fun testSetState() {
        store.set {
            copy(count = 2)
        }
        store.get {
            assertEquals(2, it.count)
        }
    }

    @Test
    fun testSubscribe() {
        assertSubscriberCalled()
        store.set { copy(count = 2) }
        assertSubscriberCalled()
    }

    @Test
    fun testSubscribeNotCalledForNoop() {
        assertSubscriberCalled()
        store.set { this }
        assertSubscriberNotCalled()
    }

    @Test
    fun testSubscribeNotCalledForSameValue() {
        assertSubscriberCalled()
        store.set { copy() }
        assertSubscriberNotCalled()
    }

    @Test
    fun testSubscribeNotCalledInInitialized() {
        lifecycleOwner.lifecycle.markState(Lifecycle.State.INITIALIZED)

        var callCount = 0
        store.subscribe(lifecycleOwner) {
            callCount++
        }

        assertEquals(0, callCount)
    }

    @Test
    fun testSubscribeNotCalledInCreated() {
        lifecycleOwner.lifecycle.markState(Lifecycle.State.CREATED)

        var callCount = 0
        store.subscribe(lifecycleOwner) {
            callCount++
        }

        assertEquals(0, callCount)
    }

    @Test
    fun testSubscribeCalledInStarted() {
        lifecycleOwner.lifecycle.markState(Lifecycle.State.STARTED)

        var callCount = 0
        store.subscribe(lifecycleOwner) {
            callCount++
        }

        assertEquals(1, callCount)
    }

    @Test
    fun testSubscribeCalledInResumed() {
        lifecycleOwner.lifecycle.markState(Lifecycle.State.RESUMED)

        var callCount = 0
        store.subscribe(lifecycleOwner) {
            callCount++
        }

        assertEquals(1, callCount)
    }

    @Test
    fun testSubscribeNotCalledInDestroyed() {
        lifecycleOwner.lifecycle.markState(Lifecycle.State.DESTROYED)

        var callCount = 0
        store.subscribe(lifecycleOwner) {
            callCount++
        }

        assertEquals(0, callCount)
    }

    @Test
    fun testSubscribeNotCalledWhenTransitionedToStopped() {
        lifecycleOwner.lifecycle.markState(Lifecycle.State.RESUMED)

        var callCount = 0
        store.subscribe(lifecycleOwner) {
            callCount++
        }

        store.set { copy(count = 2) }

        lifecycleOwner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)

        store.set { copy(count = 3) }

        assertEquals(2, callCount)
    }

    @Test
    fun testSubscribeNotCalledWhenTransitionedToDestroyed() {
        lifecycleOwner.lifecycle.markState(Lifecycle.State.RESUMED)

        var callCount = 0
        store.subscribe(lifecycleOwner) {
            callCount++
        }

        store.set { copy(count = 2) }

        lifecycleOwner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)

        store.set { copy(count = 3) }

        assertEquals(2, callCount)
    }

    @Test
    fun testSubscribeCalledWhenTransitionToStarted() {
        lifecycleOwner.lifecycle.markState(Lifecycle.State.CREATED)

        var callCount = 0
        store.subscribe(lifecycleOwner) {
            callCount++
        }

        assertEquals(0, callCount)
        lifecycleOwner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        assertEquals(1, callCount)
    }

    @Test
    fun testSubscribeCalledWhenTransitionToResumed() {
        lifecycleOwner.lifecycle.markState(Lifecycle.State.STARTED)

        var callCount = 0
        store.subscribe(lifecycleOwner) {
            callCount++
        }

        store.set { copy(count = 2) }

        lifecycleOwner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        store.set { copy(count = 3) }

        assertEquals(3, callCount)
    }

    @Test
    fun testAddToList() {
        var callCount = 0
        store.subscribe {
            callCount++
        }
        assertEquals(1, callCount)

        store.set { copy(list = list + 5) }

        assertEquals(2, callCount)
    }

    @Test
    fun testReplace() {
        var callCount = 0
        store.subscribe {
            callCount++
        }
        assertEquals(1, callCount)

        store.set { copy(list = listOf(5)) }

        assertEquals(2, callCount)
    }

    @Test
    fun testChangeValue() {
        var callCount = 0
        store.subscribe {
            callCount++
        }
        assertEquals(1, callCount)

        store.set { copy(list = listOf(5)) }

        assertEquals(2, callCount)

        store.set { copy(list = list.toMutableList().apply { set(0, 3) }) }

        assertEquals(3, callCount)
    }

    @Test
    fun testGettingAroundImmutabilityDoesntWork() {
        assertSubscriberCalled()
        store.set { copy(list = ArrayList<Int>().apply { add(5) }) }
        assertSubscriberCalled()
        // This is bad. Don't do this. Your subscribers won't get called.
        store.set { copy(list = (list as ArrayList<Int>).apply { set(0, 3) }) }
        assertSubscriberNotCalled()
    }

    private fun assertSubscriberCalled() {
        assertEquals(lastAssertedCallCount + 1, callCount)
        lastAssertedCallCount = callCount
    }

    private fun assertSubscriberNotCalled() {
        assertEquals(lastAssertedCallCount, callCount)
        lastAssertedCallCount = callCount
    }
}