package com.airbnb.mvrx.hellodagger.di

import com.airbnb.mvrx.hellodagger.HelloDaggerViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface AppModule {

    @Binds
    @IntoMap
    @ViewModelKey(HelloDaggerViewModel::class)
    fun helloViewModelFactory(factory: HelloDaggerViewModel.Factory): AssistedViewModelFactory<*, *>
}
