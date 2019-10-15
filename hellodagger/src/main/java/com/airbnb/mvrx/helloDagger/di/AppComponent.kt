package com.airbnb.mvrx.helloDagger.di

import com.airbnb.mvrx.helloDagger.base.BaseActivity
import dagger.Component

@Component(modules = [AppModule::class])
interface AppComponent {

    fun inject(activity: BaseActivity)

}