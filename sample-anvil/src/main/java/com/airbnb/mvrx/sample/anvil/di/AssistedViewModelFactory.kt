package com.airbnb.mvrx.sample.anvil.di

import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel

/**
 * You do not need to use this type directly. It is used by DaggerMavericksViewModelFactory.
 *
 * Serves as a supertype for AssistedInject factories in ViewModels.
 *
 * We use this interface as a marker in a Multibinding Dagger setup to populate a Map
 * of ViewModel classes with their AssistedInject factories.
 *
 * First we define our ViewModel with an @AssistedInject annotated constructor, and a Factory interface
 * implementing AssistedViewModelFactory.
 *
 * class MyViewModel @AssistedInject constructor(
 *     @Assisted initialState: MyState,
 *     …
 * ): MavericksViewModel<MyState>(...) {
 *   @AssistedFactory
 *   interface Factory : AssistedViewModelFactory<MyViewModel, MyState> {
 *     override fun create(state: MyState): MyViewModel
 *   }
 * }
 *
 * Then we need to create a Dagger Module, which contains methods that @Binds @IntoMap all of our
 * AssistedViewModelFactories using a [ViewModelKey]. Notice that the input to these methods is
 * the exact type of our AssistedInject factory, but the return type is an AssistedViewModelFactory.
 *
 * @Module
 * interface MyAppModule {
 *   @Binds
 *   @IntoMap
 *   @ViewModelKey(MyViewModel::class)
 *   fun myViewModelFactory(factory: MyViewModel.Factory): AssistedViewModelFactory<*, *>
 * }
 *
 * This Module tells Dagger to include MyViewModel.Factory class in the Multibinding map using
 * MyViewModel::class as the key. Such a method should be added for **every ViewModel Factory**
 * so that they can be identified by Dagger and used for populating the Map.
 *
 * The generated map can then be injected wherever it is required.
 *
 * interface AppComponent {
 *   fun viewModelFactories(): Map<Class<out BaseViewModel<*>>, AssistedViewModelFactory<*, *>>
 * }
 *
 * class SomeClass @Inject constructor(
 *   val viewModelFactories: Map<Class<out BaseViewModel<*>>, AssistedViewModelFactory<*, *>>
 * )
 */
interface AssistedViewModelFactory<VM : MavericksViewModel<S>, S : MavericksState> {
    fun create(state: S): VM
}
