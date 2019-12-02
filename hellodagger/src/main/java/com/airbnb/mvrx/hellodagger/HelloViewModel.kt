package com.airbnb.mvrx.hellodagger

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.hellodagger.base.BaseViewModel
import com.airbnb.mvrx.hellodagger.di.AssistedViewModelFactory
import com.airbnb.mvrx.hellodagger.di.DaggerMvRxViewModelFactory
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