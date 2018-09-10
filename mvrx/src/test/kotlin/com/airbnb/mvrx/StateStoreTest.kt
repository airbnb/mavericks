package com.airbnb.mvrx

import android.arch.lifecycle.Lifecycle
import dalvik.annotation.TestTarget
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.ActorScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.*
import org.junit.rules.TestRule
import org.junit.Rule



data class StateStoreTestState(val count: Int = 1, val list: List<Int> = emptyList())


@RunWith(Parameterized::class)
class StateStoreTest(val param: Any) : BaseTest() {
    
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Array<Any> {
            return Array(1000) { 0 }
        }
    }

    private lateinit var store: IMvRxStateStore<StateStoreTestState>

    @Before
    fun setup() {
        store = MvCorStateStore(StateStoreTestState(),Unconfined)
    }

     @Test
     fun testGetRunsSynchronouslyForTests() {
         var callCount = 0
         store.get { callCount++ }
         assertEquals(1, callCount)
     }

     @Test
     fun testSetState()  {
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
     fun testSubscribeNotCalledForNoop()  {
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


    @Test
    fun testConcurrency() = runBlocking {

        List(5000) {
            launch {
                store.set {
                    copy(count + 1)
                }
                store.get {
                    store.state
                }
            }
        }.joinAll()

        assertEquals(5001, store.state.count)
    }
}


