# Dagger Usage Sample for MvRx

This module contains a sample app to demonstrate how to ease the usage of Dagger and AssistedInject in apps using MvRx.

## Key Features

* **Injecting state into ViewModels with AssistedInject**
  
  Since the `initialState` parameter is only available at runtime, Dagger can not provide this dependency for us. We need the [AssistedInject](https://github.com/square/AssistedInject) library for this purpose.

* **Multibinding setup for AssistedInject Factories**

  Every ViewModel using AssistedInject needs a Factory interface annotated with `@AssistedInject.Factory`. These factories are grouped together under a common parent type [AssistedViewModelFactory](src/main/java/com/airbnb/mvrx/hellodagger/di/AssistedViewModelFactory.kt) to enable a Multibinding Dagger setup.

* **Removing boilerplate from a MvRxViewModelFactory**

  An `MvRxViewModelFactory` is different than an AssistedInject Factory, and is still needed. Using this AssistedInject multibinding setup, most ViewModels will share the same boilerplate logic in their `MvRxViewModelFactory`'s. A [DaggerMvRxViewModelFactory](src/main/java/com/airbnb/mvrx/hellodagger/di/DaggerMvRxViewModelFactory.kt) has been added to eliminate this boilerplate.

## Example

* Create your ViewModel with an @AssistedInject constructor, an AssistedInject Factory implementing `AssistedViewModelFactory`, and a companion object implementing `DaggerMvRxViewModelFactory`.

```kotlin
class MyViewModel @AssistedInject constructor(
  @Assisted initialState: MyState,
  // and other dependencies
) {

  @AssistedInject.Factory
  interface Factory: AssistedViewModelFactory<MyViewModel, MyState> {
    override fun create(initialState: MyState): MyViewModel
  }

  companion object: DaggerMvRxViewModelFactory(MyViewModel::class.java)
}
```

* Tell Dagger to include your ViewModel's AssistedInject Factory in a Multibinding map.

```kotlin
interface AppModule {

    @Binds
    @IntoMap
    @ViewModelKey(MyViewModel::class)
    fun myViewModelFactory(factory: MyViewModel.Factory): AssistedViewModelFactory<*, *>

}
```

* Add a provision for the Multibinding map in your Dagger component:

```kotlin
    fun viewModelFactories(): Map<Class<out BaseViewModel<*>>, AssistedViewModelFactory<*, *>>
```

* With this setup complete, request your ViewModel in a Fragment as usual, using any of MvRx's ViewModel delegates.

```kotlin
class MyFragment : BaseMvRxFragment() {
  val viewModel: MyViewModel by fragmentViewModel()
}
```

## How it works

The `DaggerMvRxViewModelFactory` is used by MvRx to create the requested ViewModel. This factory uses the map of AssistedInject factories provided in the `AppComponent`, retrieves the one for the requested ViewModel and delegates the creation to it.
