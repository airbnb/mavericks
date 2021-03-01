package com.airbnb.mvrx.sample.utils

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Create bindings for a view similar to bindView.
 *
 * To use, just call
 * private val binding: FHomeWorkoutDetailsBinding by viewBinding()
 * with your binding class and access it as you normally would.
 */
inline fun <reified T : ViewBinding> Fragment.viewBinding() = FragmentViewBindingDelegate(T::class.java, this)

class FragmentViewBindingDelegate<T : ViewBinding>(
    private val bindingClass: Class<T>,
    val fragment: Fragment
) : ReadOnlyProperty<Fragment, T> {
    private var binding: T? = null

    private val bindMethod = bindingClass.getMethod("bind", View::class.java)

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        binding?.let { return it }

        val lifecycle = fragment.viewLifecycleOwner.lifecycle
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            error("Cannot access view bindings. View lifecycle is ${lifecycle.currentState}!")
        }

        fragment.viewLifecycleOwnerLiveData.observe(
            fragment,
            Observer { viewLifecycleOwner ->
                viewLifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {
                    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                    fun onDestroy() {
                        binding = null
                    }
                })
            }
        )

        @Suppress("UNCHECKED_CAST")
        binding = bindMethod.invoke(null, thisRef.requireView()) as T
        return binding!!
    }
}
