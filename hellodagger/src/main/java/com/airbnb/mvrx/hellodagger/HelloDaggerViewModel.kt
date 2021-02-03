package com.airbnb.mvrx.hellodagger

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.hellodagger.di.AssistedViewModelFactory
import com.airbnb.mvrx.hellodagger.di.daggerMavericksViewModelFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

data class HelloDaggerState(val message: Async<String> = Uninitialized) : MavericksState

class HelloDaggerViewModel @AssistedInject constructor(
    @Assisted state: HelloDaggerState,
    private val repo: HelloRepository
) : MavericksViewModel<HelloDaggerState>(state) {

    init {
        sayHello()
    }

    fun sayHello() {
        repo.sayHello().execute { copy(message = it) }
    }

    @AssistedFactory
    interface Factory : AssistedViewModelFactory<HelloDaggerViewModel, HelloDaggerState> {
        override fun create(state: HelloDaggerState): HelloDaggerViewModel
    }

    companion object : MavericksViewModelFactory<HelloDaggerViewModel, HelloDaggerState> by daggerMavericksViewModelFactory()
}
