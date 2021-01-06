package com.airbnb.mvrx.hellohilt.di

import com.airbnb.mvrx.hellohilt.HelloViewModel
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap

/**
 * The InstallIn SingletonComponent scope must match the context scope used in [HiltMavericksViewModelFactory]
 *
 * If you want an Activity or Fragment scope
 */
@Module
@InstallIn(SingletonComponent::class)
interface AppModule {

    @[Binds IntoMap ViewModelKey(HelloViewModel::class)]
    fun helloViewModelFactory(factory: HelloViewModel.Factory): AssistedViewModelFactory<*, *>
}
