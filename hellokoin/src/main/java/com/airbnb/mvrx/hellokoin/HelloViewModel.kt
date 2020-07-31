package com.airbnb.mvrx.hellokoin

import android.util.Log
import com.airbnb.mvrx.*
import com.airbnb.mvrx.hellokoin.base.BaseViewModel
import org.koin.core.context.KoinContextHandler
import org.koin.core.parameter.parametersOf

data class HelloState(
        @PersistState val counter: Int = 0,
        val message: Async<String> = Uninitialized
) : MvRxState

class HelloViewModel constructor(
    /*@Assisted*/ state: HelloState,
    private val repo: HelloRepository
) : BaseViewModel<HelloState>(state) {

//    init {
//        sayHello()
//    }

    fun sayHello() {
        repo.sayHello().execute {
            copy(message = it, counter = counter + 1)
        }
    }

    companion object : MvRxViewModelFactory<HelloViewModel, HelloState> {
        override fun create(viewModelContext: ViewModelContext, state: HelloState): HelloViewModel? {
            Log.i("State", "Initial state: $state")
            val koin = KoinContextHandler.get()
            return koin.get<HelloViewModel> {
                parametersOf(state)
            }
        }
    }
}