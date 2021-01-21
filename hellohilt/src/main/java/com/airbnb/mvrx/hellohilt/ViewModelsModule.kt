package com.airbnb.mvrx.hellohilt

import com.airbnb.mvrx.hellohilt.di.AssistedViewModelFactory
import com.airbnb.mvrx.hellohilt.di.MavericksViewModelComponent
import com.airbnb.mvrx.hellohilt.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.multibindings.IntoMap

@Module
@InstallIn(MavericksViewModelComponent::class)
interface ViewModelsModule {
    @Binds
    @IntoMap
    @ViewModelKey(HelloHiltViewModel::class)
    fun helloViewModelFactory(factory: HelloHiltViewModel.Factory): AssistedViewModelFactory<*, *>
}
