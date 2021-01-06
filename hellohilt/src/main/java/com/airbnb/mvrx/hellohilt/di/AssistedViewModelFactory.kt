package com.airbnb.mvrx.hellohilt.di

import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MvRxState

/*
 * Serves as a supertype for AssistedInject factories in ViewModels.
 *
 * We can use this interface as a marker in a Multibinding Dagger setup to populate a Map
 * of ViewModel classes with their AssistedInject factories.
 *
 * This setup is needed because AssistedInject factories are isolated from each other, so this
 * interface provides a common parent type to facilitate grouping these factories into a collection.
 * When we use it correctly with Dagger, a Map of ViewModels and their factories can be created for us
 * automatically. This Map can then be used to retrieve for a ViewModel's factory using its class.
 *
 * Here's an example for such a setup:
 *
 * First we define our ViewModel with an @AssistedInject annotated constructor, and a Factory interface
 * implementing AssistedViewModelFactory.
 *
 * class MyViewModel @AssistedInject constructor(...): BaseMvRxViewModel<MyState>(...) {
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
 * @AssistedModule(includes = [AssistedInject_MyAppModule::class])
 * @Module
 * interface MyAppModule {
 *   @[Binds IntoMap ViewModelKey(MyViewModel::class)]
 *   fun myViewModelFactory(factory: MyViewModel.Factory): AssistedViewModelFactory<*, *>
 * }
 *
 * This Module tells Dagger to include MyViewModel.Factory class in the Multibinding map using
 * MyViewModel::class as the key. Such a method should be added for **every ViewModel Factory**
 * so that they can be identified by Dagger and used for populating the Map.
 *
 * The generated map can then be injected wherever it is required.
 *
 * @EntryPoint
 * @InstallIn(SingletonComponent::class) // ActivityComponent::class or FragmentComponent::class if required.
 * interface HiltMavericksEntryPoint {
 *  val viewModelFactories: Map<Class<out MavericksViewModel<*>>, AssistedViewModelFactory<*, *>>
 * }
 *
 * **NOTE**: If you want multiple `EntryPoint`s for different scopes then your @[Binds ViewModelKey(clazz:class)] will need also have a @Qualifier.
 *
 * class SomeClass @Inject constructor(
 *   val viewModelFactories: Map<Class<out MavericksViewModel<*>>, AssistedViewModelFactory<*, *>>
 * )
 */
interface AssistedViewModelFactory<VM : MavericksViewModel<S>, S : MvRxState> {
    fun create(state: S): VM
}
