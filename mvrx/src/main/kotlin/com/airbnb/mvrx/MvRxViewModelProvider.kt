package com.airbnb.mvrx

import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.RestrictTo
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import java.io.Serializable
import java.lang.IllegalStateException

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
     * @param forExistingViewModel If true the viewModel should already have been created. If it has not been created already,
     *                             a [ViewModelDoesNotExistException] will be thrown
     * @param initialStateFactory A way to specify how to create the initial state, can be mocked out for testing.
     *
     */
    fun <VM : BaseMvRxViewModel<S>, S : MvRxState> get(
        viewModelClass: Class<out VM>,
        stateClass: Class<out S>,
        viewModelContext: ViewModelContext,
        key: String = viewModelClass.name,
        forExistingViewModel: Boolean = false,
        initialStateFactory: MvRxStateFactory<VM, S> = RealMvRxStateFactory()
    ): VM {
        val viewModel = ViewModelProvider(
            viewModelContext.owner,
            MvRxFactory(viewModelClass, stateClass, viewModelContext, key, forExistingViewModel, initialStateFactory)
        ).get(key, viewModelClass)

        with(viewModelContext.savedStateRegistry) {
            unregisterSavedStateProvider(key)
            registerSavedStateProvider(key) {
                viewModel.getSavedStateBundle(viewModelContext)
            }
        }

        return viewModel
    }

    private fun <VM : BaseMvRxViewModel<S>, S : MvRxState> VM.getSavedStateBundle(viewModelContext: ViewModelContext) =
        withState(this) { state ->
            Bundle().apply {
                putBundle(KEY_MVRX_SAVED_INSTANCE_STATE, state.persistState())
                viewModelContext.args?.let {
                    when (it) {
                        is Parcelable -> putParcelable(KEY_MVRX_SAVED_ARGS, it)
                        is Serializable -> putSerializable(KEY_MVRX_SAVED_ARGS, it)
                        else -> throw IllegalStateException("Args must be parcelable or serializable")
                    }
                }

            }
        }
}

/**
 * Return the [Class] of the [MvRxViewModelFactory] for a given ViewModel class, if it exists.
 */
internal fun <VM : BaseMvRxViewModel<*>> Class<VM>.factoryCompanion(): Class<out MvRxViewModelFactory<VM, *>>? {
    val companionClass = try {
        Class.forName("$name\$Companion")
    } catch (exception: ClassNotFoundException) {
        return null
    }
    return if (MvRxViewModelFactory::class.java.isAssignableFrom(companionClass)) {
        @Suppress("UNCHECKED_CAST")
        companionClass as Class<out MvRxViewModelFactory<VM, *>>
    } else {
        null
    }
}

/**
 * Given a companion class, use Java reflection to create an instance. This is used over
 * Kotlin reflection for performance.
 */
internal fun Class<*>.instance(): Any {
    return declaredConstructors.first { it.parameterTypes.size == 1 }.newInstance(null)
}
