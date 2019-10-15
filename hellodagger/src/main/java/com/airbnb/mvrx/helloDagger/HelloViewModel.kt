package com.airbnb.mvrx.helloDagger

import com.airbnb.mvrx.*
import com.airbnb.mvrx.helloDagger.di.AssistedViewModelFactory
import com.airbnb.mvrx.helloDagger.base.BaseViewModel
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

    companion object : MvRxViewModelFactory<HelloViewModel, HelloState> {
        override fun create(viewModelContext: ViewModelContext, state: HelloState): HelloViewModel? {
            return createViewModel(viewModelContext.activity, state)
        }
    }
}