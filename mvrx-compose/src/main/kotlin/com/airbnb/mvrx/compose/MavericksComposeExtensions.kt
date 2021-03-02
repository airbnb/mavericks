package com.airbnb.mvrx.compose

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.airbnb.mvrx.ActivityViewModelContext
import com.airbnb.mvrx.FragmentViewModelContext
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
 */
@Composable
inline fun <reified VM : MavericksViewModel<S>, reified S : MavericksState> mavericksViewModel(
    scope: LifecycleOwner = LocalLifecycleOwner.current,
    noinline keyFactory: (() -> String)? = null,
): VM {
    val activity = LocalContext.current as? ComponentActivity ?: error("Composable is not hosted in a ComponentActivity!")
    val viewModelStoreOwner = scope as? ViewModelStoreOwner ?: error("LifecycleOwner must be a ViewModelStoreOwner!")
    val savedStateRegistryOwner = scope as? SavedStateRegistryOwner ?: error("LifecycleOwner must be a SavedStateRegistryOwner!")
    val savedStateRegistry = savedStateRegistryOwner.savedStateRegistry
    val viewModelClass = VM::class
    val viewModelContext = remember(scope, activity, viewModelStoreOwner, savedStateRegistry) {
        when (scope) {
            is Fragment -> {
                val args = scope.arguments?.get(Mavericks.KEY_ARG)
                FragmentViewModelContext(activity, args, scope)
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
fun <VM : MavericksViewModel<S>, S : MavericksState> VM.collectAsState(): State<S> {
    return stateFlow.collectAsState(initial = withState(this) { it })
}

/**
 * Creates a Compose State variable that will emit new values whenever this ViewModel's state mapped to the provided mapper changes.
 * Prefer the overload with a state property reference to ensure that your composable only recomposes when the properties it uses changes.
 */
@Composable
fun <VM : MavericksViewModel<S>, S : MavericksState, O> VM.collectAsState(mapper: (S) -> O): State<O> {
    return stateFlow.map { mapper(it) }.distinctUntilChanged().collectAsState(initial = withState(this) { mapper(it) })
}

/**
 * Creates a Compose State variable that will only update when the value of this property changes.
 * Prefer this to subscribing to entire state classes which will trigger a recomposition whenever any state variable changes.
 * If you find yourself subscribing to many state properties in a single composable, consider breaking it up into smaller ones.
 */
@Composable
fun <VM : MavericksViewModel<S>, S : MavericksState, A> VM.collectAsState(prop1: KProperty1<S, A>): State<A> {
    return stateFlow.map { prop1.get(it) }.distinctUntilChanged().collectAsState(initial = withState(this) { prop1.get(it) })
}
