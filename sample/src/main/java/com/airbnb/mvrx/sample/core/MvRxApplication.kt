package com.airbnb.mvrx.sample.core

import android.app.Application
import com.airbnb.mvrx.sample.di.AppComponent
import com.airbnb.mvrx.sample.di.DaggerAppComponent

class MvRxApplication : Application() {
    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.create()
    }
}