# Dagger Usage Sample for Mavericks

This module contains a sample app to demonstrate how to ease the usage of Dagger and AssistedInject in apps using Mavericks.

## Key Features

* **Injecting state into ViewModels with AssistedInject**
  
  Since the `initialState` parameter is only available at runtime, we use Dagger [assisted injection](https://dagger.dev/dev-guide/assisted-injection).

* **Multibinding setup for Dagger**

  Every ViewModel using Dagger [assisted injection](https://dagger.dev/dev-guide/assisted-injection) needs a Factory interface annotated with `@AssistedFactory`. These factories are grouped together under a common parent type [AssistedViewModelFactory](src/main/java/com/airbnb/mavericks/hellodagger/di/AssistedViewModelFactory.kt) to enable a Multibinding Dagger setup.

* **Removing boilerplate from a MavericksViewModelFactory**

  An `MavericksViewModelFactory` is different than an AssistedInject Factory, and is still needed. Using this AssistedInject multibinding setup, most ViewModels will share the same boilerplate logic in their `MavericksViewModelFactory`'s. A [DaggerMavericksViewModelFactory](https://github.com/airbnb/mavericks/blob/master/hellodagger/src/main/java/com/airbnb/mvrx/hellodagger/di/DaggerMavericksViewModelFactory.kt) has been added to eliminate this boilerplate.

## Example

* Create your ViewModel with an @AssistedInject constructor, an AssistedInject Factory implementing `AssistedViewModelFactory`, and a companion object implementing `DaggerMavericksViewModelFactory`.

```kotlin
class MyViewModel @AssistedInject constructor(
  @Assisted initialState: MyState,
  // and other dependencies
) {

  @AssistedFactory
  interface Factory: AssistedViewModelFactory<MyViewModel, MyState> {
    override fun create(initialState: MyState): MyViewModel
  }

  companion object: DaggerMavericksViewModelFactory(MyViewModel::class.java)
}
```

* Tell Dagger to include your ViewModel's AssistedInject Factory in a Multibinding map.

```kotlin
@Module
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

* With this setup complete, request your ViewModel in a Fragment as usual, using any of Mavericks' ViewModel delegates.

```kotlin
class MyFragment : Fragment(), MavericksView {
  val viewModel: MyViewModel by fragmentViewModel()
}
```

## How it works

The `DaggerMavericksViewModelFactory` is used by Mavericks to create the requested ViewModel. This factory uses the map of AssistedInject factories provided in the `AppComponent`, retrieves the one for the requested ViewModel and delegates the creation to it.
