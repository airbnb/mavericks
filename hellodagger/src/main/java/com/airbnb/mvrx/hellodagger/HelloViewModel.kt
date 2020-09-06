package com.airbnb.mvrx.hellodagger

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.hellodagger.di.AssistedViewModelFactory
import com.airbnb.mvrx.hellodagger.di.DaggerMavericksViewModelFactory
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

data class HelloState(val message: Async<String> = Uninitialized) : MavericksState

class HelloViewModel @AssistedInject constructor(
    @Assisted state: HelloState,
    private val repo: HelloRepository
) : MavericksViewModel<HelloState>(state) {

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

    companion object : DaggerMavericksViewModelFactory<HelloViewModel, HelloState>(HelloViewModel::class.java)
}
