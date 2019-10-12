package com.airbnb.mvrx.helloDagger

import dagger.Component

@Component(modules = [AppModule::class])
interface AppComponent {

    fun helloViewModelFactory(): HelloViewModel.Factory

}