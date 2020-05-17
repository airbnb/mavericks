package com.airbnb.mvrx

import org.junit.Test

data class RxSetupState(val foo: Int = 0) : MvRxState

class RxSetupTest : BaseTest() {

    @Test(expected = IllegalArgumentException::class)
    fun ensureExceptionsThrownInRxJavaAreCaught() {
        class RxSetupViewModel(initialState: RxSetupState) : TestMavericksViewModel<RxSetupState>(initialState) {
            fun throwInWithState() {
                withState {
                    throw IllegalArgumentException("Intentional Exception")
                }
            }
        }
        RxSetupViewModel(RxSetupState()).throwInWithState()
    }
}
