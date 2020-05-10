package com.airbnb.mvrx

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.savedstate.SavedStateRegistryOwner

/**
 * A class that allows for delegation of lifecycle and process death semantics to an external
 * [savedStateRegistryOwner], and the invalidation of the view to a generic [onInvalidated] function.
 *
 * This is most useful in cases where inheriting from BaseMvRxFragment is
 * unfeasible, undesirable, or for composing with a custom view.
 */
class DelegateMvRxView(
        private val savedStateRegistryOwner: SavedStateRegistryOwner,
        override val subscriptionLifecycleOwner: LifecycleOwner = savedStateRegistryOwner,
        private val onInvalidated: () -> Unit
) : MvRxView {

    private val name: String = savedStateRegistryOwner.javaClass.name
    private val mvrxViewIdProperty = MvRxViewId()

    init {
        savedStateRegistryOwner.savedStateRegistry.registerSavedStateProvider(name) { Bundle().apply { mvrxViewIdProperty.saveTo(this) } }
        savedStateRegistryOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) = when (event) {
                Lifecycle.Event.ON_CREATE -> {
                    mvrxViewId // Read from the delegate to init the unique id.
                    mvrxViewIdProperty.restoreFrom(savedStateRegistryOwner.savedStateRegistry.consumeRestoredStateForKey(name))
                }
                Lifecycle.Event.ON_START -> {
                    // This ensures that invalidate() is called for static screens that don't
                    // subscribe to a ViewModel.
                    postInvalidate()
                }
                Lifecycle.Event.ON_DESTROY -> {
                    source.lifecycle.removeObserver(this)
                    savedStateRegistryOwner.savedStateRegistry.unregisterSavedStateProvider(name)
                }
                else -> Unit
            }
        })
    }

    override val mvrxViewId by mvrxViewIdProperty

    override fun invalidate() = onInvalidated()

    override fun getLifecycle(): Lifecycle = savedStateRegistryOwner.lifecycle
}
