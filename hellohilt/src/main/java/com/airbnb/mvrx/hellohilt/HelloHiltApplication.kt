package com.airbnb.mvrx.hellohilt

import android.app.Application
import com.airbnb.mvrx.Mavericks
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HelloHiltApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Mavericks.initialize(this)
    }
}
