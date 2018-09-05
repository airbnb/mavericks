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
        store = MvCorStateStore(StateStoreTestState())
    }

     @Test
     fun testGetRunsSynchronouslyForTests() = runBlocking {
         var callCount = 0
         store.get { callCount++ }
         delay(10)
         assertEquals(1, callCount)
     }

     @Test
     fun testSetState()  = runBlocking {
         store.set {
             copy(count = 2)
         }
         var called = false
         delay(10)
         store.get {
             assertEquals(2, it.count)
             called = true
         }
         delay(10)
         assertTrue(called)
     }

     @Test
     fun testSubscribeNotCalledForNoop() = runBlocking {
         var callCount = 0
         store.observable.subscribe {
             callCount++
         }
         delay(5)
         assertEquals(1, callCount)
         store.set { this }
         delay(5)
         assertEquals(1, callCount)
     }


     @Test
     fun testSubscribeNotCalledForSameValue() = runBlocking {
         var callCount = 0
         store.observable.subscribe {
             callCount++
         }
         delay(10)
         assertEquals(1, callCount)
         store.set { copy() }
         delay(5)
         assertEquals(1, callCount)
     }


    @Test
    fun testConcurrency() = runBlocking {

        val cur = System.currentTimeMillis()
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

        while(store.state.count<5001 && cur+50>System.currentTimeMillis()) {

        }
        assertEquals(5001, store.state.count)
    }
}


