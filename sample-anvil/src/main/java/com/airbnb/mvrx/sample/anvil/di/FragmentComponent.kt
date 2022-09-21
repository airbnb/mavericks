package com.airbnb.mvrx.sample.anvil.di

import android.app.Application
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.airbnb.mvrx.sample.anvil.AnvilSampleApplication
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.ConcurrentHashMap

inline fun <reified T : Any> Fragment.fragmentComponent(crossinline factory: (CoroutineScope, AnvilSampleApplication) -> T) = lazy {
    ViewModelProvider(this)[DaggerComponentHolderViewModel::class.java].get<T>(factory)
}

class DaggerComponentHolderViewModel(app: Application) : AndroidViewModel(app) {
    val map = ConcurrentHashMap<Class<*>, Any>()

    inline fun <reified T> get(factory: (CoroutineScope, AnvilSampleApplication) -> T): T {
        return map.getOrPut(T::class.java) { factory(viewModelScope, getApplication()) } as T
    }
}
