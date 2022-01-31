package com.airbnb.mvrx.hilt

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.multibindings.IntoMap

@Module
@InstallIn(MavericksViewModelComponent::class)
interface TestViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(TestViewModel::class)
    fun testViewModelFactory(factory: TestViewModel.Factory): AssistedViewModelFactory<*, *>
}
