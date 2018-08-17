package com.airbnb.mvrx

import io.reactivex.exceptions.CompositeException
import org.junit.Test

data class RxSetupState(val foo: Int = 0) : MvRxState

class RxSetupTest : BaseTest() {

    @Test(expected = CompositeException::class)
    fun ensureExceptionsThrownInRxJavaAreCaught() {
        class RxSetupViewModel(override val initialState: RxSetupState) : TestMvRxViewModel<RxSetupState>() {
            fun throwInWithState() {
                withState {
                    throw IllegalArgumentException()
                }
            }
        }
        RxSetupViewModel(RxSetupState()).throwInWithState()
    }
}