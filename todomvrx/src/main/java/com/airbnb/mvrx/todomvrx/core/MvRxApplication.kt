package com.airbnb.mvrx.todomvrx.core

import android.app.Application
import com.airbnb.mvrx.todomvrx.di.AppComponent
import com.airbnb.mvrx.todomvrx.di.DaggerAppComponent

class MvRxApplication : Application() {
    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.builder().application(this).build()
    }
}