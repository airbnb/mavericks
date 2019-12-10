package com.airbnb.mvrx.hellodagger.di

import com.airbnb.mvrx.hellodagger.base.BaseViewModel
import dagger.Component

@Component(modules = [AppModule::class])
interface AppComponent {
    fun viewModelFactories(): Map<Class<out BaseViewModel<*>>, AssistedViewModelFactory<*, *>>
}