package com.airbnb.mvrx

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity

/**
 * Implement this on your ViewModel's companion object for hooks into state creation and ViewModel creation. For example, if you need access
 * to the fragment or activity owner for dependency injection.
 */
interface MvRxViewModelFactory<VM: BaseMvRxViewModel<S>, S : MvRxState> {

    /**
     * @param viewModelContext [ViewModelContext] which contains the ViewModel owner and arguments.
     * @param state The initial state to pass to the ViewModel. In a new process, state will have all [PersistState] annotated members restored,
     * therefore you should never create a custom state in this method. To customize the initial state, override [initialState].
     *
     * @return The ViewModel. If you return `null` the ViewModel must have a single argument
     * constructor only taking the initial state.
     */
    fun create(viewModelContext: ViewModelContext, state: S): VM? = null

    /**
     * The initial state for the ViewModel. Override this if the initial state requires information from arguments, or the ViewModel owner.
     * This function will take precedence over any secondary constructors defined in the state class, [S].
     *
     * The return value of this function will be transformed with any [PersistState] values before being used in [create].
     *
     * @return the initial state. If `null`, the state class constructors will be used for initial state creation.
     */
    fun initialState(viewModelContext: ViewModelContext): S? = null

}

/**
 * Creation context for the ViewModel. Includes the ViewModel store owner (either an activity or fragment), and fragment arguments
 * set via [MvRx.KEY_ARG].
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
    abstract val activity: FragmentActivity
    /**
     * Fragment arguments set via [MvRx.KEY_ARG].
     */
    abstract val rawArgs: Any?

    /**
     * Convenience method to type [rawArgs].
     */
    abstract fun <A> args(): A
}

/**
 * The [ViewModelContext] for a ViewModel created with an
 * activity scope (`val viewModel by activityViewModel<MyViewModel>`).
 */
class ActivityViewModelContext(
        override val activity: FragmentActivity,
        override val rawArgs: Any?
) : ViewModelContext() {
    override fun <A> args(): A {
        @Suppress("UNCHECKED_CAST")
        return rawArgs as A
    }
}

/**
 * The [ViewModelContext] for a ViewModel created with a
 * fragment scope (`val viewModel by fragmentViewModel<MyViewModel>`).
 */
class FragmentViewModelContext(
        override val activity: FragmentActivity,
        override val rawArgs: Any?,
        val fragment: Fragment
) : ViewModelContext() {
    override fun <A> args(): A {
        @Suppress("UNCHECKED_CAST")
        return rawArgs as A
    }
}
