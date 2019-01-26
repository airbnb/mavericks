package com.airbnb.mvrx

import android.arch.lifecycle.ViewModelProviders
import android.support.annotation.RestrictTo
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
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
        viewModelClass: Class<VM>,
        stateClass: Class<S>,
        viewModelContext: ViewModelContext,
        key: String = viewModelClass.name
    ): VM {
        // This wraps the fact that ViewModelProvider.of has individual methods for Fragment and FragmentActivity.
        val factory = MvRxFactory { createViewModel(viewModelClass, stateClass, viewModelContext) }
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
        stateRestorer: (S) -> S = { it }
    ): VM {
        val initialState = createInitialState(viewModelClass, stateClass, viewModelContext, stateRestorer)
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
                "${MvRxViewModelFactory::class.java.simpleName} must have primary constructor with a " +
                    "single parameter that takes initial state of ${stateClass.simpleName}."
            }
        }
    }

    /**
     * Given a companion class, use Java reflection to create an instance. This is used over
     * Kotlin reflection for performance.
     */
    private fun Class<*>.instance(): Any {
        return declaredConstructors.first { it.parameterTypes.size == 1 }.newInstance(null)
    }

    private fun <VM : BaseMvRxViewModel<S>, S : MvRxState> createInitialState(
        viewModelClass: Class<out VM>,
        stateClass: Class<out S>,
        viewModelContext: ViewModelContext,
        stateRestorer: (S) -> S
    ): S {
        @Suppress("UNCHECKED_CAST")
        val factoryState = viewModelClass.factoryCompanion()?.let { factoryClass ->
            try {
                factoryClass.getMethod("initialState", ViewModelContext::class.java)
                    .invoke(factoryClass.instance(), viewModelContext) as S?
            } catch (exception: NoSuchMethodException) {
                // Check for JvmStatic method.
                viewModelClass.getMethod("initialState", ViewModelContext::class.java)
                    .invoke(null, viewModelContext) as S?
            }
        }
        return stateRestorer(factoryState
            ?: createStateFromConstructor(stateClass, viewModelContext.args))
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

    /**
     * Return the [Class] of the [MvRxViewModelFactory] for a given ViewModel class, if it exists.
     */
    private fun <VM : BaseMvRxViewModel<*>> Class<VM>.factoryCompanion() : Class<out MvRxViewModelFactory<VM, *>>? {
        val companionClass = try {
            Class.forName("$name\$Companion")
        } catch (exception: ClassNotFoundException) {
            null
        }
        return if (companionClass != null && MvRxViewModelFactory::class.java.isAssignableFrom(companionClass)) {
            @Suppress("UNCHECKED_CAST")
            companionClass as Class<out MvRxViewModelFactory<VM, *>>
        } else {
            null
        }
    }

    /**
     *
     * Searches [stateClass] for a single argument constructor matching the type of [args]. If [args] is null, then
     * no arg constructor is invoked.
     *
     */
    @Suppress("FunctionName") // Public for inline.
    internal fun <S : MvRxState> createStateFromConstructor(stateClass: Class<S>, args: Any?): S {
        val argsConstructor = args?.let { arg ->
            val argType = arg::class.java

            stateClass.constructors.firstOrNull { constructor ->
                constructor.parameterTypes.size == 1 && isAssignableTo(argType, constructor.parameterTypes[0])
            }
        }

        @Suppress("UNCHECKED_CAST")
        return argsConstructor?.newInstance(args) as? S
            ?: try {
                stateClass.newInstance()
            } catch (@Suppress("TooGenericExceptionCaught") e: Throwable) {
                null
            }
            ?: throw IllegalStateException(
                "Attempt to create the MvRx state class ${stateClass.simpleName} has failed. One of the following must be true:" +
                    "\n 1) The state class has default values for every constructor property." +
                    "\n 2) The state class has a secondary constructor for ${args?.javaClass?.simpleName
                        ?: "a fragment argument"}." +
                    "\n 3) The ViewModel using the state must have a companion object implementing MvRxFactory with an initialState function " +
                    "that does not return null. "
            )
    }
}
