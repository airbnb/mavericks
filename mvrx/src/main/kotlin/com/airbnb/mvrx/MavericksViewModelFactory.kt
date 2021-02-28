package com.airbnb.mvrx

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry

/**
 * Implement this on your ViewModel's companion object for hooks into state creation and ViewModel creation. For example, if you need access
 * to the fragment or activity owner for dependency injection.
 */
interface MavericksViewModelFactory<VM : MavericksViewModel<S>, S : MavericksState> {

    /**
     * @param viewModelContext [ViewModelContext] which contains the ViewModel owner and arguments.
     * @param state The initial state to pass to the ViewModel. In a new process, state will have all [PersistState] annotated members restored,
     * therefore you should never create a custom state in this method. To customize the initial state, override [initialState].
     *
     * @return The ViewModel. If you return `null` the ViewModel must have a single argument
     * constructor only taking the initial state.
     */
    @Suppress("Detekt.FunctionOnlyReturningConstant")
    fun create(viewModelContext: ViewModelContext, state: S): VM? = null

    /**
     * The initial state for the ViewModel. Override this if the initial state requires information from arguments, or the ViewModel owner.
     * This function will take precedence over any secondary constructors defined in the state class, [S].
     *
     * The return value of this function will be transformed with any [PersistState] values before being used in [create].
     *
     * @return the initial state. If `null`, the state class constructors will be used for initial state creation.
     */
    @Suppress("Detekt.FunctionOnlyReturningConstant")
    fun initialState(viewModelContext: ViewModelContext): S? = null
}

/**
 * Creation context for the ViewModel. Includes the ViewModel store owner (either an activity or fragment), and fragment arguments
 * set via [Mavericks.KEY_ARG].
 *
 * For activity scoped ViewModels see [ActivityViewModelContext].
 * For fragment scoped ViewModels see [FragmentViewModelContext].
 *
 * Never store a reference to this context, or the activity/fragment.
 *
 */
sealed class ViewModelContext {
    /**
     * The activity which is using the ViewModel.
     */
    abstract val activity: ComponentActivity

    internal abstract val savedStateRegistry: SavedStateRegistry
    internal abstract val owner: ViewModelStoreOwner

    /**
     * Convenience method to type [activity].
     */
    @Suppress("UNCHECKED_CAST")
    fun <A : ComponentActivity> activity(): A = activity as A

    /**
     * Convenience method to access a typed Application.
     */
    @Suppress("UNCHECKED_CAST")
    fun <A : Application> app(): A = activity.application as A

    /**
     * Fragment arguments set via [Mavericks.KEY_ARG].
     */
    abstract val args: Any?

    /**
     * Convenience method to type [args].
     */
    @Suppress("UNCHECKED_CAST")
    fun <A> args(): A = args as A
}

/**
 * The [ViewModelContext] for a ViewModel created with an activity scope (`val viewModel by activityViewModel<MyViewModel>`). Although a fragment
 * reference is available when an activity scoped ViewModel is first created, during process restoration, activity scoped ViewModels will be created
 * _without_ a fragment reference, so it is only safe to reference the activity.
 */
data class ActivityViewModelContext(
    override val activity: ComponentActivity,
    override val args: Any?,
    override val owner: ViewModelStoreOwner = activity,
    override val savedStateRegistry: SavedStateRegistry = activity.savedStateRegistry,
) : ViewModelContext()

/**
 * The [ViewModelContext] for a ViewModel created with a
 * fragment scope (`val viewModel by fragmentViewModel<MyViewModel>`).
 *
 * The [owner] and [savedStateRegistry] default to the Fragment as the provider, if you need to use an alternative
 * source for the [ViewModelStoreOwner] and/or [SavedStateRegistry]; such in the case of JetPack Navigation Component destinations with ViewModels,
 * then you can provided them via this context.
 */
data class FragmentViewModelContext(
    override val activity: ComponentActivity,
    override val args: Any?,
    /**
     * The fragment owner of the ViewModel.
     */
    val fragment: Fragment,
    override val owner: ViewModelStoreOwner = fragment,
    override val savedStateRegistry: SavedStateRegistry = fragment.savedStateRegistry
) : ViewModelContext() {

    /**
     * Convenience method to type [fragment].
     */
    @Suppress("UNCHECKED_CAST")
    fun <F : Fragment> fragment(): F = fragment as F
}
