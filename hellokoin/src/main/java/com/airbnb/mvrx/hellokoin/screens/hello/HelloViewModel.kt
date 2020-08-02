package com.airbnb.mvrx.hellokoin.screens.hello

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.PersistState
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.hellokoin.HelloRepository
import com.airbnb.mvrx.hellokoin.base.BaseViewModel
import com.airbnb.mvrx.hellokoin.di.KoinMvRxViewModelFactory

data class HelloState(
    @PersistState val counter: Int = 0,
    val message: Async<String> = Uninitialized
) : MvRxState

class HelloViewModel constructor(
    state: HelloState,
    private val repo: HelloRepository
) : BaseViewModel<HelloState>(state) {

    init {
        sayHello()
    }

    fun sayHello() {
        repo.sayHello().execute {
            copy(message = it, counter = counter + 1)
        }
    }

    companion object : KoinMvRxViewModelFactory<HelloViewModel, HelloState>(HelloViewModel::class.java)
}