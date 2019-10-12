package com.airbnb.mvrx.helloDagger

import com.airbnb.mvrx.*
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

data class HelloState(val message: Async<String> = Uninitialized) : MvRxState

class HelloViewModel @AssistedInject constructor(
        @Assisted initialState: HelloState,
        private val repo: HelloRepository
) : BaseMvRxViewModel<HelloState>(initialState, BuildConfig.DEBUG) {

    init {
        sayHello()
    }

    fun sayHello() {
        repo.sayHello().execute { copy(message = it) }
    }

    @AssistedInject.Factory
    interface Factory {
        fun create(initialState: HelloState): HelloViewModel
    }

    companion object: MvRxViewModelFactory<HelloViewModel, HelloState> {
        override fun create(viewModelContext: ViewModelContext, state: HelloState): HelloViewModel? {
            return viewModelContext.appComponent().helloViewModelFactory().create(state)
        }
    }
}