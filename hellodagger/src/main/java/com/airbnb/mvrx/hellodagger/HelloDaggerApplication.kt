package com.airbnb.mvrx.hellodagger

import android.app.Application
import androidx.fragment.app.FragmentActivity
import com.airbnb.mvrx.hellodagger.di.AppComponent
import com.airbnb.mvrx.hellodagger.di.DaggerAppComponent
import com.airbnb.mvrx.mock.MvRxMocks

class HelloDaggerApplication : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.create()
        MvRxMocks.install(this)
    }
}

fun FragmentActivity.appComponent(): AppComponent {
    return (application as HelloDaggerApplication).appComponent
}
