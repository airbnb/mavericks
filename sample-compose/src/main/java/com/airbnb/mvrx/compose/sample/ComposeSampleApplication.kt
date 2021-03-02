package com.airbnb.mvrx.compose.sample

import android.app.Application
import com.airbnb.mvrx.Mavericks

class ComposeSampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Mavericks.initialize(this)
    }
}
