package com.airbnb.mvrx

import android.content.Context
import android.content.pm.ApplicationInfo

object MvRx {
    /**
     * If your initial state needs to use Fragment arguments, store your arguments
     * as a parcelable class stored at this key.
     */
    const val KEY_ARG = "mvrx:arg"

    /**
     * A factory that provides the Lazy ViewModels created for MvRx extension functions,
     * such as [activityViewModel].
     *
     * Each time a ViewModel is accessed via one of these extension functions this factory
     * creates the Kotlin property delegate that wraps the ViewModel.
     *
     * The default implementation [DefaultViewModelDelegateFactory] is fine for general usage,
     * but a custom factory may be provided to assist with testing, or if you want control
     * over how and when ViewModels and their State are created.
     */
    var viewModelDelegateFactory: ViewModelDelegateFactory = DefaultViewModelDelegateFactory()

}

internal val Context.isDebuggable: Boolean
    get() = 0 != (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE)