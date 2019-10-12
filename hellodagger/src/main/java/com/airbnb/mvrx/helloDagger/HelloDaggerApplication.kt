package com.airbnb.mvrx.helloDagger

import android.app.Application
import com.airbnb.mvrx.ViewModelContext

class HelloDaggerApplication: Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.create()
    }

}

fun ViewModelContext.appComponent(): AppComponent {
    return this.app<HelloDaggerApplication>().appComponent
}