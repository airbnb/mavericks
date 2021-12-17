package com.airbnb.mvrx.compose

import android.app.Application
import com.airbnb.mvrx.Mavericks

class TestComposeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Mavericks.initialize(this)
    }
}
