package com.airbnb.mvrx.hellokoin.di

import com.airbnb.mvrx.hellokoin.*
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