package com.airbnb.mvrx.hellokoin

import android.app.Application
import com.airbnb.mvrx.hellokoin.di.allModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class HelloKoinApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@HelloKoinApplication)
            modules(allModules)
        }
    }
}