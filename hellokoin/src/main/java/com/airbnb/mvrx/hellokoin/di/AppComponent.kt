package com.airbnb.mvrx.hellokoin.di

import com.airbnb.mvrx.hellokoin.base.BaseViewModel
import dagger.Component

@Component(modules = [AppModule::class])
interface AppComponent {
    fun viewModelFactories(): Map<Class<out BaseViewModel<*>>, AssistedViewModelFactory<*, *>>
}