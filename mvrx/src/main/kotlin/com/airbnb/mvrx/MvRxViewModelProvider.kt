package com.airbnb.mvrx

import android.arch.lifecycle.ViewModelProviders
import android.arch.lifecycle.ViewModelStoreOwner
import android.support.annotation.RestrictTo
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.primaryConstructor

/**
 * Helper ViewModelProvider that has a single method for taking either a [Fragment] or [FragmentActivity] instead
 * of two separate ones. The logic for providing the correct scope is inside the method.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
object MvRxViewModelProvider {
    /**
     * MvRx specific ViewModelProvider used for creating a BaseMvRxViewModel scoped to either a [Fragment] or [FragmentActivity].
     * If this is in a [Fragment], it cannot be called before the Fragment has been added to an Activity or wrapped in a [Lazy] call.
     *
     * @param viewModelClass The class of the ViewModel you would like an instance of
     * @param storeOwner Either a [Fragment] or [FragmentActivity] to be the scope owner of the ViewModel. Activity scoped ViewModels
     *                   can be used to share state across Fragments.
     * @param key An optional key for the ViewModel in the store. This is optional but should be used if you have multiple of the same
     *            ViewModel class in the same scope.
     * @param stateFactory A factory to create the initial state if the ViewModel does not yet exist.
     *
     * The ViewModel stateFactory is lazily queried so you can safely use things like intent extras as long as you don't try and use your ViewModel
     * until after all of your parameters are available.
     *
     * You can also supply a vararg whitelist of state property to receive updates for. The property can be specified with
     * property syntax: `YourState::yourProperty`
     */
    fun <VM : BaseMvRxViewModel<S>, S : MvRxState> get(
        viewModelClass: KClass<VM>,
        storeOwner: ViewModelStoreOwner,
        key: String = viewModelClass.java.name,
        stateFactory: () -> S
    ): VM {
        // This wraps the fact that ViewModelProvider.of has individual methods for Fragment and FragmentActivity.
        val activityOwner = storeOwner as? FragmentActivity
        val fragmentOwner = storeOwner as? Fragment
        val fragmentActivity = activityOwner
                ?: fragmentOwner?.activity
                ?: throw IllegalArgumentException("$storeOwner must either be an Activity or a Fragment that is attached to an Activity")

        val factory = MvRxFactory {
            createFactoryViewModel(viewModelClass, fragmentActivity, stateFactory()) ?: createDefaultViewModel(viewModelClass, stateFactory())
        }
        return when {
            activityOwner != null -> ViewModelProviders.of(activityOwner, factory)
            else -> ViewModelProviders.of(fragmentOwner!!, factory)
        }.get(key, viewModelClass.java)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <VM : BaseMvRxViewModel<*>> createFactoryViewModel(
        viewModelClass: KClass<VM>,
        fragmentActivity: FragmentActivity,
        state: MvRxState
    ) = (viewModelClass.companionObjectInstance as? MvRxViewModelFactory<MvRxState>)?.create(fragmentActivity, state)

    fun <VM : BaseMvRxViewModel<*>> createDefaultViewModel(viewModelClass: KClass<VM>, state: Any): VM {
        val primaryConstructor = requireNotNull(viewModelClass.primaryConstructor) {
            "$viewModelClass must implement a companion object BaseMvRxViewModelFactory or have a primary constructor " +
                    "that takes state as a single arg."
        }

        require(!primaryConstructor.parameters[0].isOptional) { "initialState may not be an optional constructor parameter." }
        return primaryConstructor.call(state)
    }
}