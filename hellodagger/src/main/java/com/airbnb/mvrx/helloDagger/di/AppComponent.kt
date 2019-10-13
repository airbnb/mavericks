package com.airbnb.mvrx.helloDagger.di

import com.airbnb.mvrx.helloDagger.HelloViewModel
import com.airbnb.mvrx.helloDagger.base.MvRxActivity
import dagger.Component

@Component(modules = [AppModule::class])
interface AppComponent {

    fun inject(activity: MvRxActivity)

    fun helloViewModelFactory(): HelloViewModel.Factory

}