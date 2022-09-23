package com.airbnb.mvrx.sample.anvil.di

import android.app.Application
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.airbnb.mvrx.sample.anvil.AnvilSampleApplication
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.ConcurrentHashMap

/**
 * Use this property delegate to create a DaggerComponent scoped to a Fragment.
 *
 * The factory lambda will be given Application as well as a CoroutineScope that will have the same lifecycle as this component.
 *
 * In the factory, return the instance of the Dagger component. Most likely, it will look something like:
 * ```
 * app.bindings<MyComponent.ParentBindings>().createMyComponent()
 * ```
 *
 * The returned component will be stored inside of a backing Jetpack ViewModel and will have the equivalent lifecycle as it.
 * That means that during configuration changes or while on the back stack, your Dagger component will continue to operate.
 * When the Fragment is destroyed for the last time (equivalent to ViewModel.onCleared()), the provided CoroutineScope will be canceled.
 *
 * It may be convenient to bind the CoroutineScope as an instance in your Dagger component so it can be injected into singleton objects.
 */
inline fun <reified T : Any> Fragment.fragmentComponent(
    crossinline factory: (CoroutineScope, AnvilSampleApplication) -> T
) = lazy {
    ViewModelProvider(this)[DaggerComponentHolderViewModel::class.java].get(factory)
}

/**
 * @see fragmentComponent
 */
class DaggerComponentHolderViewModel(app: Application) : AndroidViewModel(app) {
    val map = ConcurrentHashMap<Class<*>, Any>()

    inline fun <reified T> get(factory: (CoroutineScope, AnvilSampleApplication) -> T): T {
        return map.getOrPut(T::class.java) { factory(viewModelScope, getApplication()) } as T
    }
}
