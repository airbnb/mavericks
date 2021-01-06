package com.airbnb.mvrx.hellohilt

import android.app.Application
import com.airbnb.mvrx.mocking.MockableMavericks
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HelloHiltApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MockableMavericks.initialize(this)
    }
}
