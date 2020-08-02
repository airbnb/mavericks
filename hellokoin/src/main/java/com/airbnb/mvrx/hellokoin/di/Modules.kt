package com.airbnb.mvrx.hellokoin.di

import com.airbnb.mvrx.hellokoin.*
import com.airbnb.mvrx.hellokoin.screens.hello.HelloState
import com.airbnb.mvrx.hellokoin.screens.hello.HelloViewModel
import com.airbnb.mvrx.hellokoin.screens.scopedhello.ScopedHelloState
import com.airbnb.mvrx.hellokoin.screens.scopedhello.ScopedHelloViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    factory { HelloRepository() }
}

val mvvmModule = module {
    viewModel { (state: HelloState) -> HelloViewModel(state, get()) }

    scope<MainActivity> {
        scoped { ScopedObject() }
        viewModel { (state: ScopedHelloState) -> ScopedHelloViewModel(state, get(), get()) }
    }
}

val allModules = appModule + mvvmModule