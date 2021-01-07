package com.airbnb.mvrx.sample.navigation

import android.app.Application
import com.airbnb.mvrx.Mavericks
import com.airbnb.mvrx.navigation.DefaultNavigationViewModelDelegateFactory

class FlowNavigationApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Mavericks.initialize(this, viewModelDelegateFactory = DefaultNavigationViewModelDelegateFactory())
    }
}
