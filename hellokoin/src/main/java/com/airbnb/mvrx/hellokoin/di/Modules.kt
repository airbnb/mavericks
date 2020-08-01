package com.airbnb.mvrx.hellokoin.di

import com.airbnb.mvrx.hellokoin.*
import org.koin.androidx.viewmodel.compat.ScopeCompat.viewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.KoinContextHandler.get
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {
    factory { HelloRepository() }
}

val mvvmModule = module {
    viewModel { (state: HelloState) -> HelloViewModel(state, get()) }

    scope<MainActivity> {
        viewModel { (state: ScopedHelloState) -> ScopedHelloViewModel(state, get()) }
    }
}

val allModules = appModule + mvvmModule