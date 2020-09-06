package com.airbnb.mvrx.todomvrx

import android.app.Application
import com.airbnb.mvrx.Mavericks

class ToDoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Mavericks.initialize(this)
    }
}
