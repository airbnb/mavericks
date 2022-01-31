package com.airbnb.mvrx.hilt

import android.app.Application
import com.airbnb.mvrx.Mavericks
import dagger.hilt.android.testing.CustomTestApplication

@CustomTestApplication(HiltTestBaseApplication::class)
interface HiltMavericksTestApplication

open class HiltTestBaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Mavericks.initialize(applicationContext)
    }
}
