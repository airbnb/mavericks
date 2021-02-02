# Hilt Usage Sample for Mavericks

This module contains a sample app demonstrating how to setup Hilt and AssistedInject in an app using Mavericks.

// build.gradle
```groovy
dependencies {
    def hiltVersion = "2.31.0" // or newer.
    kapt "com.google.dagger:hilt-android-compiler:${hiltVersion}"
    implementation "com.google.dagger:hilt-android:${hiltVersion}"
}
```

## Key Features

* **Injecting state into ViewModels with AssistedInject**
  
  Since the `initialState` parameter is only available at runtime, we use the [AssistedInject](https://dagger.dev/dev-guide/assisted-injection).

* **Multibinding setup for AssistedInject Factories**

  Every ViewModel using AssistedInject needs a Factory interface annotated with `@AssistedFactory`. These factories are grouped together under a common parent type [AssistedViewModelFactory](src/main/java/com/airbnb/mvrx/hellohilt/di/AssistedViewModelFactory.kt) to enable a Multibinding Dagger setup.

## Example

* Create your ViewModel with an `@AssistedInject` constructor, an `@AssistedFactory` implementing `AssistedViewModelFactory`, and a companion object implementing `MavericksViewModelFactory`.

```kotlin
// NOTE: unlike using Jetpack ViewModels with Hilt, you do not need to annotate your ViewModel class with @HiltViewModel.
class MyViewModel @AssistedInject constructor(
  @Assisted initialState: MyState,
  // and other dependencies
) {

  @AssistedFactory
  interface Factory: AssistedViewModelFactory<MyViewModel, MyState> {
    override fun create(initialState: MyState): MyViewModel
  }

  companion object : MavericksViewModelFactory<MyViewModel, MyState> by hiltMavericksViewModelFactory()
}
```

* Tell Hilt to include your ViewModel's AssistedInject Factory in a Multibinding map.

```kotlin
@Module
@InstallIn(MavericksViewModelComponent::class)
interface ViewModelsModule {
    @Binds
    @IntoMap
    @ViewModelKey(HelloHiltViewModel::class)
    fun helloViewModelFactory(factory: HelloHiltViewModel.Factory): AssistedViewModelFactory<*, *>
}

```

* With this setup complete, request your ViewModel in a Fragment as usual, using any of Mavericks's ViewModel delegates.

```kotlin
class MyFragment : Fragment(), MavericksView {
  val viewModel: MyViewModel by fragmentViewModel()
}
```

## How it works

`HiltMavericksViewModelFactory` will create a custom ViewModelComponent that is a child of SingletonComponent and will create an instance of your ViewModel with it.
