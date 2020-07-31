package com.airbnb.mvrx.hellokoin.di

import com.airbnb.mvrx.hellokoin.HelloViewModel
import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@AssistedModule
@Module(includes = [AssistedInject_AppModule::class])
interface AppModule {

    @Binds
    @IntoMap
    @ViewModelKey(HelloViewModel::class)
    fun helloViewModelFactory(factory: HelloViewModel.Factory): AssistedViewModelFactory<*, *>
}