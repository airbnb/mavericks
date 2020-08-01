package com.airbnb.mvrx.hellokoin

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.PersistState
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.hellokoin.base.BaseViewModel
import com.airbnb.mvrx.hellokoin.di.KoinMvRxViewModelFactory

data class ScopedHelloState(
        @PersistState val counter: Int = 0,
        val message: Async<String> = Uninitialized
) : MvRxState

class ScopedHelloViewModel constructor(
    state: ScopedHelloState,
    private val repo: HelloRepository
) : BaseViewModel<ScopedHelloState>(state) {

    init {
        sayHello()
    }

    fun sayHello() {
        repo.sayHello().execute {
            copy(message = it, counter = counter + 1)
        }
    }

    companion object : KoinMvRxViewModelFactory<ScopedHelloViewModel, ScopedHelloState>(ScopedHelloViewModel::class.java)
}