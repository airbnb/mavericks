package com.airbnb.mvrx.sample.anvil.di

import com.airbnb.mvrx.FragmentViewModelContext
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext

/**
 * To connect Mavericks ViewModel creation with Anvil's dependency injection, add the following to your MavericksViewModel.
 *
 * Example:
 *
 * @ContributesViewModel(YourScope::class)
 * class MyViewModel @AssistedInject constructor(
 *     @Assisted initialState: MyState,
 *     …,
 * ): MavericksViewModel<MyState>(...) {
 *     …
 *
 *     companion object : MavericksViewModelFactory<MyViewModel, MyState> by daggerMavericksViewModelFactory()
 * }
 */

inline fun <reified VM : MavericksViewModel<S>, S : MavericksState> daggerMavericksViewModelFactory() = DaggerMavericksViewModelFactory<VM, S>(VM::class.java)

/**
 * A [MavericksViewModelFactory] makes it easy to create instances of a ViewModel
 * using its AssistedInject Factory. This class should be implemented by the companion object
 * of every ViewModel which uses AssistedInject via [daggerMavericksViewModelFactory].
 *
 * @param viewModelClass The [Class] of the ViewModel being requested for creation
 *
 * This class accesses the map of ViewModel class to [AssistedViewModelFactory]s from the nearest [DaggerComponentOwner] and
 * uses it to retrieve the requested ViewModel's factory class. It then creates an instance of this ViewModel
 * using the retrieved factory and returns it.
 * @see daggerMavericksViewModelFactory
 */
class DaggerMavericksViewModelFactory<VM : MavericksViewModel<S>, S : MavericksState>(
    private val viewModelClass: Class<VM>
) : MavericksViewModelFactory<VM, S> {

    override fun create(viewModelContext: ViewModelContext, state: S): VM {
        val bindings: DaggerMavericksBindings = when (viewModelContext) {
            is FragmentViewModelContext -> viewModelContext.fragment.bindings()
            else -> viewModelContext.activity.bindings()
        }
        val viewModelFactoryMap = bindings.viewModelFactories()
        val viewModelFactory = viewModelFactoryMap[viewModelClass] ?: error("Cannot find ViewModelFactory for ${viewModelClass.name}.")

        @Suppress("UNCHECKED_CAST")
        val castedViewModelFactory = viewModelFactory as? AssistedViewModelFactory<VM, S>
        val viewModel = castedViewModelFactory?.create(state)
        return viewModel as VM
    }
}

/**
 * These Anvil/Dagger bindings are used by [DaggerMavericksViewModelFactory]. The factory will find the nearest [DaggerComponentOwner]
 * that implements these bindings. It will then attempt to retrieve the [AssistedViewModelFactory] for the given ViewModel class.
 *
 * In this example, this bindings class is implemented by [com.airbnb.mvrx.sample.anvil.feature.ExampleFeatureComponent] because
 * it provides the [com.airbnb.mvrx.sample.anvil.feature.ExampleFeatureViewModel]. Any component that will generate ViewModels should
 * either implement this directly or have this added via `@ContributesTo(YourScope::class)`.
 */
interface DaggerMavericksBindings {
    fun viewModelFactories(): Map<Class<out MavericksViewModel<*>>, AssistedViewModelFactory<*, *>>
}
