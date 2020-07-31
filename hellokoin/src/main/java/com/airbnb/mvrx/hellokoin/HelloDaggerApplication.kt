package com.airbnb.mvrx.hellokoin

import android.app.Application
import androidx.fragment.app.FragmentActivity
import com.airbnb.mvrx.hellokoin.di.AppComponent
import com.airbnb.mvrx.hellokoin.di.DaggerAppComponent

class HelloDaggerApplication : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.create()
    }
}

fun FragmentActivity.appComponent(): AppComponent {
    return (application as HelloDaggerApplication).appComponent
}
