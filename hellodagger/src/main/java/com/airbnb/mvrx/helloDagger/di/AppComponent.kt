package com.airbnb.mvrx.helloDagger.di

import com.airbnb.mvrx.helloDagger.base.BaseViewModel
import dagger.Component

@Component(modules = [AppModule::class])
interface AppComponent {

    fun viewModelFactories(): Map<Class<out BaseViewModel<*>>, AssistedViewModelFactory<*, *>>

}