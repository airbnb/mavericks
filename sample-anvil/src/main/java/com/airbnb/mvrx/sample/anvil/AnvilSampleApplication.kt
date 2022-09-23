package com.airbnb.mvrx.sample.anvil

import android.app.Application
import com.airbnb.mvrx.Mavericks
import com.airbnb.mvrx.sample.anvil.di.DaggerComponentOwner
import com.airbnb.mvrx.sample.anvil.di.bindings

class AnvilSampleApplication : Application(), DaggerComponentOwner {

    lateinit var appComponent: AppComponent
    // This can be set or unset as users log in and out.
    var userComponent: UserComponent? = null

    override val daggerComponent get() = listOfNotNull(appComponent, userComponent)

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.create()
        // Simulate a logged in user
        userComponent = bindings<UserComponent.ParentBindings>().userComponentBuilder().user(User("Gabriel Peal")).build()
        Mavericks.initialize(this)
    }
}
