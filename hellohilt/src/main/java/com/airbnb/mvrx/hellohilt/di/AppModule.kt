package com.airbnb.mvrx.hellohilt.di

import com.airbnb.mvrx.hellohilt.HelloViewModel
import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.migration.DisableInstallInCheck
import dagger.multibindings.IntoMap

@AssistedModule
@Module(includes = [AssistedInject_AppModule::class])
@DisableInstallInCheck
@InstallIn(ApplicationComponent::class)
interface AppModule {

    @Binds
    @IntoMap
    @ViewModelKey(HelloViewModel::class)
    fun helloViewModelFactory(factory: HelloViewModel.Factory): AssistedViewModelFactory<*, *>
}
