package com.airbnb.mvrx.hellokoin

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.hellokoin.base.BaseViewModel
import com.airbnb.mvrx.hellokoin.di.AssistedViewModelFactory
import com.airbnb.mvrx.hellokoin.di.DaggerMvRxViewModelFactory
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

data class HelloState(val message: Async<String> = Uninitialized) : MvRxState

class HelloViewModel @AssistedInject constructor(
    @Assisted state: HelloState,
    private val repo: HelloRepository
) : BaseViewModel<HelloState>(state) {

    init {
        sayHello()
    }

    fun sayHello() {
        repo.sayHello().execute { copy(message = it) }
    }

    @AssistedInject.Factory
    interface Factory : AssistedViewModelFactory<HelloViewModel, HelloState> {
        override fun create(state: HelloState): HelloViewModel
    }

    companion object : DaggerMvRxViewModelFactory<HelloViewModel, HelloState>(HelloViewModel::class.java)
}