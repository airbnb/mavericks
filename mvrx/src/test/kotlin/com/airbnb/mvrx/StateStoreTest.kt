package com.airbnb.mvrx

import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

data class MvRxStateStoreTestState(val count: Int = 1, val list: List<Int> = emptyList())

@RunWith(Parameterized::class)
class StateStoreTest(val rx: Boolean) : BaseTest() {

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data() = listOf(
                false,
                true
        )
    }


    private lateinit var store: MvRxStateStore<MvRxStateStoreTestState>

    @Before
    fun setup() {
        store = if (rx) RealMvRxStateStore(MvRxStateStoreTestState()) else MvCorStateStore(MvRxStateStoreTestState(), Dispatchers.Unconfined)
    }

    @Test
    fun testGetRunsSynchronouslyForTests() {
        var callCount = 0
        store.get { callCount++ }
        assertEquals(1, callCount)
    }

    @Test
    fun testSetState() {
        store.set {
            copy(count = 2)
        }
        var called = false
        store.get {
            assertEquals(2, it.count)
            called = true
        }
        assertTrue(called)
    }

    @Test
    fun testSubscribeNotCalledForNoop() {
        var callCount = 0
        store.observable.subscribe {
            callCount++
        }
        assertEquals(1, callCount)
        store.set { this }
        assertEquals(1, callCount)
    }

    @Test
    fun testSubscribeNotCalledForSameValue() {
        var callCount = 0
        store.observable.subscribe {
            callCount++
        }
        assertEquals(1, callCount)
        store.set { copy() }
        assertEquals(1, callCount)
    }
}