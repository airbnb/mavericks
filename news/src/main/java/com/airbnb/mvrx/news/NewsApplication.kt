package com.airbnb.mvrx.news

import android.app.Application
import com.airbnb.mvrx.news.di.AppComponent
import com.airbnb.mvrx.news.di.DaggerAppComponent

class NewsApplication : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent
                .factory()
                .create(this)
    }
}