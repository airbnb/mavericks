package com.airbnb.mvrx

import org.junit.Test

data class RxSetupState(val foo: Int = 0) : MvRxState

class RxSetupTest : BaseTest() {

    @Test(expected = IllegalArgumentException::class)
    fun ensureExceptionsThrownInRxJavaAreCaught() {
        class RxSetupViewModel(initialState: RxSetupState) : TestMvRxViewModel<RxSetupState>(initialState) {
            fun throwInWithState() {
                withState {
                    println("withState thread ${Thread.currentThread().id}")
                    throw IllegalArgumentException("Intentional Exception")
                }
            }
        }
        println("Test thread ${Thread.currentThread().id}")
        RxSetupViewModel(RxSetupState()).throwInWithState()
    }
}