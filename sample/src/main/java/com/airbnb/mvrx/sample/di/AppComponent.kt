package com.airbnb.mvrx.sample.di

import com.airbnb.mvrx.sample.core.BaseFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, ViewModelModule::class])
interface AppComponent {
    fun inject(fragment: BaseFragment)
}
