package com.airbnb.mvrx

import android.arch.lifecycle.ViewModelProviders
import android.arch.lifecycle.ViewModelStoreOwner
import android.support.annotation.RestrictTo
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
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
     */
    fun <VM : BaseMvRxViewModel<S>, S : MvRxState> get(
        viewModelClass: Class<VM>,
        storeOwner: ViewModelStoreOwner,
        key: String = viewModelClass.name,
        stateFactory: () -> S
    ): VM {
        // This wraps the fact that ViewModelProvider.of has individual methods for Fragment and FragmentActivity.
        val activityOwner = storeOwner as? FragmentActivity
        val fragmentOwner = storeOwner as? Fragment

        val factory = MvRxFactory {
            when {
                activityOwner != null -> createViewModel(viewModelClass, activityOwner, stateFactory())
                fragmentOwner != null -> createViewModel(viewModelClass, fragmentOwner, stateFactory())
                else -> throw IllegalArgumentException("$storeOwner must either be an Activity or a Fragment that is attached to an Activity")
            }
        }
        return when {
            activityOwner != null -> ViewModelProviders.of(activityOwner, factory)
            else -> ViewModelProviders.of(fragmentOwner!!, factory)
        }.get(key, viewModelClass)
    }

    @Suppress("UNCHECKED_CAST")
    fun <VM : BaseMvRxViewModel<S>, S : MvRxState> createViewModel(
        viewModelClass: Class<VM>,
        fragmentActivity: FragmentActivity,
        state: S
    ): VM = createFactoryViewModel(viewModelClass, fragmentActivity, state)
        .let { validateViewModel(it, viewModelClass, state) }

    @Suppress("UNCHECKED_CAST")
    fun <VM : BaseMvRxViewModel<S>, S : MvRxState> createViewModel(
        viewModelClass: Class<VM>,
        fragment: Fragment,
        state: S
    ): VM = createFactoryViewModel(viewModelClass, fragment, state)
        .let { validateViewModel(it, viewModelClass, state) }

    private fun <VM : BaseMvRxViewModel<S>, S : MvRxState> validateViewModel(
        viewModel: VM?,
        viewModelClass: Class<VM>,
        state: S
    ): VM {
        return requireNotNull(viewModel) {

            // We are about to crash, so accessing Kotlin reflect is okay for a better error message.
            when {
                viewModelClass.kotlin.companionObjectInstance is MvRxViewModelFactory<*> -> {
                    "${viewModelClass.simpleName} companion " +
                        "${MvRxViewModelFactory::class.java.simpleName} is missing ${JvmStatic::class.java.name} " +
                        "annotation on its create method."
                }
                viewModelClass.kotlin.companionObjectInstance != null -> {
                    "${viewModelClass.simpleName} must have primary constructor with a single " +
                        "parameter for initial state of ${state::class.java} or a companion object " +
                        "implementing ${MvRxViewModelFactory::class.java} and a ${JvmStatic::class.java.simpleName} " +
                        "annotated create method. Found a companion object which does not " +
                        "implement ${MvRxViewModelFactory::class.java.simpleName}."
                }
                viewModelClass.kotlin.primaryConstructor?.parameters?.size?.let { it > 1 } == true -> {
                    "${viewModelClass.simpleName} takes dependencies other than initialState. " +
                        "It must have companion object implementing ${MvRxViewModelFactory::class.java.simpleName} " +
                        "and a ${JvmStatic::class.java.simpleName} annotated create method."
                }
                viewModelClass.kotlin.primaryConstructor?.parameters?.size?.let { it == 0 } == true -> {
                    "${MvRxViewModelFactory::class.java.simpleName} must have primary constructor with a " +
                        "single parameter that takes initial state of ${state::class.java.simpleName}."
                }
                viewModelClass.kotlin.primaryConstructor?.parameters?.get(0)?.type != state::class -> {
                    "${MvRxViewModelFactory::class.java.simpleName} must have primary constructor with a " +
                        "single parameter that takes initial state of ${state::class.java.simpleName}. Found type " +
                        "${viewModelClass.kotlin.primaryConstructor?.parameters?.get(0)?.type?.javaClass?.simpleName}"
                }
                viewModelClass.kotlin.primaryConstructor?.parameters?.get(0)?.isOptional == true -> {
                    "initialState may not be an optional constructor parameter."
                }
                else -> {
                    "${viewModelClass.simpleName} must have a companion object implementing " +
                        "${MvRxViewModelFactory::class.java.simpleName} and a ${JvmStatic::class.java.simpleName} " +
                        "annotated create method *or* have primary constructor with a single parameter for " +
                        "initial state of ${state::class.java.simpleName}."
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <VM : BaseMvRxViewModel<S>, S : MvRxState> createFactoryViewModel(
        viewModelClass: Class<VM>,
        fragment: Fragment,
        state: S
    ): VM? {
        val method = try {
            viewModelClass.getMethod("create", Fragment::class.java, state::class.java)
        } catch (exception: NoSuchMethodException) {
            // No fragment create found check for activity create.
            return createFactoryViewModel(viewModelClass, fragment.requireActivity(), state)
        }
        val viewModel = method?.invoke(null, fragment, state) as? VM
        return viewModel ?: createDefaultViewModel(viewModelClass, state)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <VM : BaseMvRxViewModel<S>, S : MvRxState> createFactoryViewModel(
        viewModelClass: Class<VM>,
        activity: FragmentActivity,
        state: S
    ): VM? {
        val method = try {
            viewModelClass.getMethod("create", FragmentActivity::class.java, state::class.java)
        } catch (exception: NoSuchMethodException) {
            null
        }
        val viewModel = method?.invoke(null, activity, state) as? VM
        return viewModel ?: createDefaultViewModel(viewModelClass, state)
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
