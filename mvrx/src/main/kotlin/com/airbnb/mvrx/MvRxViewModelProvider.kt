package com.airbnb.mvrx

import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.RestrictTo
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import java.io.Serializable
import java.lang.IllegalArgumentException
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
        val savedStateRegistry = viewModelContext.savedStateRegistry

        if (!savedStateRegistry.isRestored) {
            error("You can only access a view model after onCreate of your component has been called.")
        }

        val stateRestorer = savedStateRegistry
            .consumeRestoredStateForKey(key)
            ?.toStateRestorer<S>(viewModelContext)

        val restoredContext = stateRestorer?.viewModelContext ?: viewModelContext

        val viewModel = ViewModelProvider(
            viewModelContext.owner,
            MvRxFactory(
                viewModelClass,
                stateClass,
                restoredContext,
                key,
                stateRestorer?.toRestoredState,
                forExistingViewModel,
                initialStateFactory
            )
        ).get(key, viewModelClass)

        try {
            // Save the view model's state to the bundle so that it can be used to recreate
            // state across system initiated process death.
            viewModelContext.savedStateRegistry.registerSavedStateProvider(key) {
                viewModel.getSavedStateBundle(restoredContext.args)
            }
        } catch (e: IllegalArgumentException) {
            // The view model was already registered with the context. We only want the initial
            // fragment that creates the view model to register with the saved state registry so
            // that it saves the correct arguments.
        }
        return viewModel
    }

    private fun <VM : BaseMvRxViewModel<S>, S : MvRxState> VM.getSavedStateBundle(
        initialArgs: Any?
    ) = withState(this) { state ->
        Bundle().apply {
            putBundle(KEY_MVRX_SAVED_INSTANCE_STATE, state.persistState())
            initialArgs?.let {
                when (it) {
                    is Parcelable -> putParcelable(KEY_MVRX_SAVED_ARGS, it)
                    is Serializable -> putSerializable(KEY_MVRX_SAVED_ARGS, it)
                    else -> error("Args must be parcelable or serializable")
                }
            }
        }
    }

    private fun <S : MvRxState> Bundle.toStateRestorer(viewModelContext: ViewModelContext): StateRestorer<S> {
        val restoredArgs = get(KEY_MVRX_SAVED_ARGS)
        val restoredState = getBundle(KEY_MVRX_SAVED_INSTANCE_STATE)

        requireNotNull(restoredState) { "State was not saved prior to restoring!" }

        val restoredContext = when (viewModelContext) {
            is ActivityViewModelContext -> viewModelContext.copy(args = restoredArgs)
            is FragmentViewModelContext -> viewModelContext.copy(args = restoredArgs)
        }
        return StateRestorer(restoredContext, restoredState::restorePersistedState)
    }

    private const val KEY_MVRX_SAVED_INSTANCE_STATE = "mvrx:saved_instance_state"
    private const val KEY_MVRX_SAVED_ARGS = "mvrx:saved_args"
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

private data class StateRestorer<S : MvRxState>(
    val viewModelContext: ViewModelContext,
    val toRestoredState: (S) -> S
)
