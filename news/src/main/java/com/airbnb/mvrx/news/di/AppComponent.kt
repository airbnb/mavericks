package com.airbnb.mvrx.news.di

import android.content.Context
import com.airbnb.mvrx.news.HeadlinesFragment
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Component(modules = [AppModule::class])
@Singleton
interface AppComponent {

    fun inject(headlinesFragment: HeadlinesFragment)

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }

}