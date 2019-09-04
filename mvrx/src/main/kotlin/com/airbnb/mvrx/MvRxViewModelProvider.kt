package com.airbnb.mvrx

import androidx.annotation.RestrictTo
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
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
     * @param viewModelClass The class of the ViewModel you would like an instance of.
     * @param stateClass The class of the State used by the ViewModel.
     * @param viewModelContext The [ViewModelContext] which contains arguments and the owner of the ViewModel.
     *                         Either [ActivityViewModelContext] or [FragmentViewModelContext].
     * @param key An optional key for the ViewModel in the store. This is optional but should be used if you have multiple of the same
     *            ViewModel class in the same scope.
     */
    fun <VM : BaseMvRxViewModel<S>, S : MvRxState> get(
        viewModelClass: Class<out VM>,
        stateClass: Class<out S>,
        viewModelContext: ViewModelContext,
        key: String = viewModelClass.name,
        initialStateFactory: MvRxStateFactory<VM, S> = RealMvRxStateFactory()
    ): VM {
        // This wraps the fact that ViewModelProvider.of has individual methods for Fragment and FragmentActivity.
        val factory = MvRxFactory { createViewModel(viewModelClass, stateClass, viewModelContext, initialStateFactory = initialStateFactory) }
        return when (viewModelContext) {
            is ActivityViewModelContext -> ViewModelProviders.of(viewModelContext.activity, factory)
            is FragmentViewModelContext -> ViewModelProviders.of(viewModelContext.fragment, factory)
        }.get(key, viewModelClass)
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <VM : BaseMvRxViewModel<S>, S : MvRxState> createViewModel(
        viewModelClass: Class<out VM>,
        stateClass: Class<out S>,
        viewModelContext: ViewModelContext,
        stateRestorer: (S) -> S = { it },
        initialStateFactory: MvRxStateFactory<VM, S> = RealMvRxStateFactory()
    ): VM {
        val initialState = initialStateFactory.createInitialState(viewModelClass, stateClass, viewModelContext, stateRestorer)
        val factoryViewModel = viewModelClass.factoryCompanion()?.let { factoryClass ->
            try {
                factoryClass.getMethod("create", ViewModelContext::class.java, MvRxState::class.java)
                    .invoke(factoryClass.instance(), viewModelContext, initialState) as VM?
            } catch (exception: NoSuchMethodException) {
                // Check for JvmStatic method.
                viewModelClass.getMethod("create", ViewModelContext::class.java, MvRxState::class.java)
                    .invoke(null, viewModelContext, initialState) as VM?
            }
        }
        val viewModel = factoryViewModel ?: createDefaultViewModel(viewModelClass, initialState)
        return requireNotNull(viewModel) {
            // If null, use Kotlin reflect for best error message. We will crash anyway, so performance
            // doesn't matter.
            if (viewModelClass.kotlin.primaryConstructor?.parameters?.size?.let { it > 1 } == true) {
                "${viewModelClass.simpleName} takes dependencies other than initialState. " +
                    "It must have companion object implementing ${MvRxViewModelFactory::class.java.simpleName} " +
                    "with a create method returning a non-null ViewModel."
            } else {
                "${viewModelClass::class.java.simpleName} must have primary constructor with a " +
                    "single non-optional parameter that takes initial state of ${stateClass.simpleName}."
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <VM : BaseMvRxViewModel<S>, S : MvRxState> createDefaultViewModel(viewModelClass: Class<VM>, state: S): VM? {
        // If we are checking for a default ViewModel, we expect only a single default constructor. Any other case
        // is a misconfiguration and we will throw an appropriate error under further inspection.
        if (viewModelClass.constructors.size == 1) {
            val primaryConstructor = viewModelClass.constructors[0]
            if (primaryConstructor.parameterTypes.size == 1 && primaryConstructor.parameterTypes[0].isAssignableFrom(state::class.java)) {
                return primaryConstructor?.newInstance(state) as? VM
            }
        }
        return null
    }
}

/**
 * Return the [Class] of the companion [MvRxViewModelFactory] for a given ViewModel class, if it exists.
 */
internal fun <VM : BaseMvRxViewModel<*>> Class<VM>.factoryCompanion(): Class<out MvRxViewModelFactory<VM, *>>? {
    return declaredClasses.firstOrNull {
        MvRxViewModelFactory::class.java.isAssignableFrom(it)
    }?.let {
        @Suppress("UNCHECKED_CAST")
        it as Class<out MvRxViewModelFactory<VM, *>>
    }
}

/**
 * Given a companion class, use Java reflection to create an instance. This is used over
 * Kotlin reflection for performance.
 */
internal fun Class<*>.instance(): Any {
    return declaredConstructors.first { it.parameterTypes.size == 1 }.newInstance(null)
}
