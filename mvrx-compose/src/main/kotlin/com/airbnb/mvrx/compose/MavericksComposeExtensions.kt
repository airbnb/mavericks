package com.airbnb.mvrx.compose

import android.content.Context
import android.content.ContextWrapper
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.savedstate.SavedStateRegistryOwner
import com.airbnb.mvrx.ActivityViewModelContext
import com.airbnb.mvrx.FragmentViewModelContext
import com.airbnb.mvrx.InternalMavericksApi
import com.airbnb.mvrx.Mavericks
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelProvider
import com.airbnb.mvrx.withState
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlin.reflect.KProperty1

/**
 * Get or create a [MavericksViewModel] scoped to the closest [LocalLifecycleOwner].
 * In most case, [LocalLifecycleOwner] will be the host Activity or Nav Graph (if used).
 * However, you can provide a custom scope by overriding the lifecycleOwner parameter.
 *
 * If you provide your own LifecycleOwner, it MUST also implement [ViewModelStoreOwner] and [SavedStateRegistryOwner]. Many standard components
 * such as [Fragment], [ComponentActivity] (and subclasses such as FragmentActivity and AppCompatActivity), and NavBackStackEntry all do.
 *
 * You can call functions on this ViewModel to update state.
 *
 * To subscribe to this view model's state, call collectAsState(YourState::yourProp), collectAsState { it.yourProp } or collectAsState() on your view model.
 *
 * @param keyFactory Optionally provide a key to differentiate multiple viewmodels of the same type in the same scope. By default the key is the ViewModel
 * java class name.
 *
 * @param argsFactory If present, the result from this function will be passed to your state constructor as a parameter when the viewmodel is first
 * initialized. This will supersede any arguments from the fragment or activity.
 */
@Composable
@Suppress("DEPRECATION")
inline fun <reified VM : MavericksViewModel<S>, reified S : MavericksState> mavericksViewModel(
    scope: LifecycleOwner = LocalLifecycleOwner.current,
    noinline keyFactory: (() -> String)? = null,
    noinline argsFactory: (() -> Any?)? = null,
): VM {
    val activity = extractActivityFromContext(LocalContext.current)
    checkNotNull(activity) {
        "Composable is not hosted in a ComponentActivity!"
    }

    val viewModelStoreOwner = scope as? ViewModelStoreOwner ?: error("LifecycleOwner must be a ViewModelStoreOwner!")
    val savedStateRegistryOwner = scope as? SavedStateRegistryOwner ?: error("LifecycleOwner must be a SavedStateRegistryOwner!")
    val savedStateRegistry = savedStateRegistryOwner.savedStateRegistry
    val viewModelClass = VM::class
    val view = LocalView.current

    val viewModelContext = remember(scope, activity, viewModelStoreOwner, savedStateRegistry) {
        val parentFragment = when (scope) {
            is Fragment -> scope
            is ComponentActivity -> null
            else -> findFragmentFromView(view)
        }

        if (parentFragment != null) {
            val args = argsFactory?.invoke() ?: parentFragment.arguments?.get(Mavericks.KEY_ARG)
            FragmentViewModelContext(activity, args, parentFragment, viewModelStoreOwner, savedStateRegistry)
        } else {
            val args = argsFactory?.invoke() ?: activity.intent.extras?.get(Mavericks.KEY_ARG)
            ActivityViewModelContext(activity, args, viewModelStoreOwner, savedStateRegistry)
        }
    }
    return remember(viewModelClass, viewModelContext) {
        MavericksViewModelProvider.get(
            viewModelClass = viewModelClass.java,
            stateClass = S::class.java,
            viewModelContext = viewModelContext,
            key = keyFactory?.invoke() ?: viewModelClass.java.name
        )
    }
}

@InternalMavericksApi
fun extractActivityFromContext(context: Context): ComponentActivity? {
    var currentContext = context
    if (currentContext is ComponentActivity) {
        return currentContext
    } else {
        while (currentContext is ContextWrapper) {
            if (currentContext is ComponentActivity) {
                return currentContext
            }
            currentContext = currentContext.baseContext
        }
    }
    return null
}

@InternalMavericksApi
fun findFragmentFromView(view: View): Fragment? = try {
    FragmentManager.findFragment(view)
} catch (_: IllegalStateException) {
    // current scope is NOT a fragment
    null
}

/**
 * Get or create a [MavericksViewModel] scoped to the local activity.
 * @see [mavericksViewModel]
 */
@Composable
inline fun <reified VM : MavericksViewModel<S>, reified S : MavericksState> mavericksActivityViewModel(
    noinline keyFactory: (() -> String)? = null,
    noinline argsFactory: (() -> Any?)? = null,
): VM {
    val activity = extractActivityFromContext(LocalContext.current)
    checkNotNull(activity) {
        "LocalContext is not a ComponentActivity!"
    }
    return mavericksViewModel(
        scope = activity,
        keyFactory = keyFactory,
        argsFactory = argsFactory
    )
}

/**
 * Creates a Compose State variable that will emit new values whenever this ViewModel's state changes.
 * Prefer the overload with a state property reference to ensure that your composable only recomposes when the properties it uses changes.
 */
@Composable
fun <VM : MavericksViewModel<S>, S : MavericksState> VM.collectAsState(): State<S> {
    return stateFlow.collectAsState(initial = withState(this) { it })
}

/**
 * Creates a Compose State variable that will emit new values whenever this ViewModel's state changes in a lifecycle-aware manner.
 * Prefer the overload with a state property reference to ensure that your composable only recomposes when the properties it uses changes.
 */
@Composable
fun <VM : MavericksViewModel<S>, S : MavericksState> VM.collectAsStateWithLifecycle(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED
): State<S> {
    return stateFlow.collectAsStateWithLifecycle(
        initialValue = withState(this) { it },
        lifecycleOwner = lifecycleOwner,
        minActiveState = minActiveState
    )
}

/**
 * Creates a Compose State variable that will emit new values whenever this ViewModel's state mapped to the provided mapper changes.
 * Prefer the overload with a state property reference to ensure that your composable only recomposes when the properties it uses changes.
 *
 * @param key An optional key that should be changed if the mapper changes. If your mapper always does the same thing, you can leave this as Unit.
 *            If your mapper changes (for example, reading a different state property) then, by default, you won't receive an updated state value
 *            until either the ViewModel emits a new state or if you change the key.
 *            This is analogous to `remember(key) { … }`.
 */
@Composable
fun <VM : MavericksViewModel<S>, S : MavericksState, O> VM.collectAsState(key: Any? = Unit, mapper: (S) -> O): State<O> {
    val updatedMapper by rememberUpdatedState(mapper)
    val mappedFlow = remember(key) { stateFlow.map { updatedMapper(it) }.distinctUntilChanged() }
    return mappedFlow.collectAsState(initial = withState(this) { updatedMapper(it) })
}

/**
 * Creates a Compose State variable that will emit new values whenever this ViewModel's state mapped to the provided mapper changes in a lifecycle-aware manner.
 * Prefer the overload with a state property reference to ensure that your composable only recomposes when the properties it uses changes.
 *
 * @param key An optional key that should be changed if the mapper changes. If your mapper always does the same thing, you can leave this as Unit.
 *            If your mapper changes (for example, reading a different state property) then, by default, you won't receive an updated state value
 *            until either the ViewModel emits a new state or if you change the key.
 *            This is analogous to `remember(key) { … }`.
 */
@Composable
fun <VM : MavericksViewModel<S>, S : MavericksState, O> VM.collectAsStateWithLifecycle(
    key: Any? = Unit,
    mapper: (S) -> O,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED
): State<O> {
    val updatedMapper by rememberUpdatedState(mapper)
    val mappedFlow = remember(key) { stateFlow.map { updatedMapper(it) }.distinctUntilChanged() }
    return mappedFlow.collectAsStateWithLifecycle(
        initialValue = withState(this) { updatedMapper(it) },
        lifecycleOwner = lifecycleOwner,
        minActiveState = minActiveState
    )
}

/**
 * Creates a Compose State variable that will only update when the value of this property changes.
 * Prefer this to subscribing to entire state classes which will trigger a recomposition whenever any state variable changes.
 * If you find yourself subscribing to many state properties in a single composable, consider breaking it up into smaller ones.
 */
@Composable
fun <VM : MavericksViewModel<S>, S : MavericksState, A> VM.collectAsState(prop1: KProperty1<S, A>): State<A> {
    val mappedFlow = remember(prop1) { stateFlow.map { prop1.get(it) }.distinctUntilChanged() }
    return mappedFlow.collectAsState(initial = withState(this) { prop1.get(it) })
}

/**
 * Creates a Compose State variable that will only update when the value of this property changes that can be collected in a lifecycle-aware manner.
 * Prefer this to subscribing to entire state classes which will trigger a recomposition whenever any state variable changes.
 * If you find yourself subscribing to many state properties in a single composable, consider breaking it up into smaller ones.
 */
@Composable
fun <VM : MavericksViewModel<S>, S : MavericksState, A> VM.collectAsStateWithLifecycle(
    prop1: KProperty1<S, A>,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED
): State<A> {
    val mappedFlow = remember(prop1) { stateFlow.map { prop1.get(it) }.distinctUntilChanged() }
    return mappedFlow.collectAsStateWithLifecycle(
        initialValue = withState(this) { prop1.get(it) },
        lifecycleOwner = lifecycleOwner,
        minActiveState = minActiveState
    )
}
