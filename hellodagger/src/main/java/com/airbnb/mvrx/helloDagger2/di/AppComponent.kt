package com.airbnb.mvrx.helloDagger2.di

import com.airbnb.mvrx.helloDagger2.base.BaseViewModel
import dagger.Component

@Component(modules = [AppModule::class])
interface AppComponent {
    fun viewModelFactories(): Map<Class<out BaseViewModel<*>>, AssistedViewModelFactory<*, *>>
}