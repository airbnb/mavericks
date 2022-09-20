package com.airbnb.mvrx.sample.anvil

import android.app.Application
import androidx.activity.ComponentActivity
import com.airbnb.mvrx.Mavericks
import com.airbnb.mvrx.anvil.AppComponent
import com.airbnb.mvrx.anvil.DaggerAppComponent

class AnvilSampleApplication : Application() {

    lateinit var appComponent: AppComponent
    // This can be set or unset as users log in and out.
    var userComponent: UserComponent? = null

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.create()
        // Simulate a logged in user
        userComponent = appComponent.userComponentBuilder().user(User("Gabriel Peal")).build()
        Mavericks.initialize(this)
    }
}

fun ComponentActivity.appComponent(): AppComponent {
    return (application as AnvilSampleApplication).appComponent
}
