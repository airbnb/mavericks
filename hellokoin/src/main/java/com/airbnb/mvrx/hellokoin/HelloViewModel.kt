package com.airbnb.mvrx.hellokoin

import android.content.ComponentCallbacks
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.airbnb.mvrx.*
import com.airbnb.mvrx.hellokoin.base.BaseViewModel
import com.airbnb.mvrx.hellokoin.di.KoinMvRxViewModelFactory
import org.koin.android.ext.android.getKoin
import org.koin.androidx.scope.lifecycleScope
import org.koin.core.context.KoinContextHandler
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.core.scope.ScopeID
import org.koin.ext.getOrCreateScope
import org.koin.ext.getScopeId
import org.koin.ext.getScopeName

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