package com.airbnb.mvrx.compose

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
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
 * Get or create a [MavericksViewModel] scoped to the closest [LocalLifecycleOwner]. This should be sufficient for most cases. By default, it
 * will be the host Activity or Nav Graph (if used). However, you can provide a custom scope by overriding the lifecycleOwner parameter.
 *
 * The ViewModelStoreOwner will default to the provided LifecycleOwner if it also implements ViewModelStoreOwner. Many LifecycleOwners such as
 * Fragment, Activity, and NavBackStackEntry do. The same applies to the SavedStateRegistry.
 *
 * If the provided LifecycleOwner doesn't implement ViewModelStoreOwner or SavedStateRegistryOwner and you haven't specified one, it will default
 * to LocalViewModelStoreOwner and LocalSavedStateRegistryOwner.
 *
 * You can call functions on this ViewModel to update state. To subscribe to its state, call collectAsState(YourState::yourProp) or collectAsState().
 */
@Composable
inline fun <reified VM : MavericksViewModel<S>, reified S : MavericksState> mavericksViewModel(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    activity: ComponentActivity = _defaultActivity(),
    viewModelStoreOwner: ViewModelStoreOwner = _defaultViewModelStoreOwner(lifecycleOwner),
    savedStateRegistryOwner: SavedStateRegistryOwner = _defaultSavedStateRegistryOwner(lifecycleOwner),
    noinline keyFactory: (() -> String)? = null,
): VM {
    val viewModelClass = VM::class
    val savedStateRegistry = savedStateRegistryOwner.savedStateRegistry
    val viewModelContext = remember(lifecycleOwner, activity, viewModelStoreOwner, savedStateRegistry) {
        when (lifecycleOwner) {
            is Fragment -> {
                val args = lifecycleOwner.arguments?.get(Mavericks.KEY_ARG)
                FragmentViewModelContext(activity, args, lifecycleOwner)
            }
            else -> {
                val args = activity.intent.extras?.get(Mavericks.KEY_ARG)
                ActivityViewModelContext(activity, args, viewModelStoreOwner, savedStateRegistry)
            }
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

/**
 * Get or create a [MavericksViewModel] scoped to the local activity.
 * @see [mavericksViewModel]
 */
@Composable
inline fun <reified VM : MavericksViewModel<S>, reified S : MavericksState> mavericksActivityViewModel(
    noinline keyFactory: (() -> String)? = null,
): VM = mavericksViewModel(
    LocalContext.current as? ComponentActivity ?: error("LocalContext is not a ComponentActivity!"),
    keyFactory = keyFactory,
)

/**
 * Creates a Compose State variable that will emit new values whenever this ViewModel's state changes.
 * Prefer the overload with a state property reference to ensure that your composable only recomposes when the properties it uses changes.
 */
@Composable
fun <VM : MavericksViewModel<S>, S : MavericksState> VM.collectState(): State<S> {
    return stateFlow.collectAsState(initial = withState(this) { it })
}

/**
 * Creates a Compose State variable that will emit new values whenever this ViewModel's state mapped to the provided mapper changes.
 * Prefer the overload with a state property reference to ensure that your composable only recomposes when the properties it uses changes.
 */
@Composable
fun <VM : MavericksViewModel<S>, S : MavericksState, O> VM.collectState(mapper: (S) -> O): State<O> {
    return stateFlow.map { mapper(it) }.distinctUntilChanged().collectAsState(initial = withState(this) { mapper(it) })
}

/**
 * Creates a Compose State variable that will only update when the value of this property changes.
 * Prefer this to subscribing to entire state classes which will trigger a recomposition whenever any state variable changes.
 * If you find yourself subscribing to many state properties in a single composable, consider breaking it up into smaller ones.
 */
@Composable
fun <VM : MavericksViewModel<S>, S : MavericksState, A> VM.collectState(prop1: KProperty1<S, A>): State<A> {
    return stateFlow.map { prop1.get(it) }.distinctUntilChanged().collectAsState(initial = withState(this) { prop1.get(it) })
}

@Composable
@InternalMavericksApi
fun _defaultActivity(): ComponentActivity {
    return LocalContext.current as? ComponentActivity ?: error("Composable is not hosted in a ComponentActivity")
}

@Composable
@InternalMavericksApi
fun _defaultViewModelStoreOwner(lifecycleOwner: LifecycleOwner): ViewModelStoreOwner {
    return when (lifecycleOwner) {
        is ViewModelStoreOwner -> lifecycleOwner
        else -> LocalViewModelStoreOwner.current
    }
}

@Composable
@InternalMavericksApi
fun _defaultSavedStateRegistryOwner(lifecycleOwner: LifecycleOwner): SavedStateRegistryOwner {
    return when (lifecycleOwner) {
        is SavedStateRegistryOwner -> lifecycleOwner
        else -> LocalSavedStateRegistryOwner.current
    }
}
