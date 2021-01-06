# Dagger Usage Sample for MvRx

This module contains a sample app demonstrating how to setup Hilt and AssistedInject in an app using MvRx.

**NOTE** Hilts bundled implementation of AssistedInject is as time of writing not released.
So, you will need to use the Snapshot release until Dagger 2.x is released with AssistedInject.

// build.gradle
buildscript {
    repositories {
        google()
        jcenter()
        maven { url "https://oss.sonatype.org/content/repositories/snapshots" }
    }
    dependencies {
        classpath "com.google.dagger:hilt-android-gradle-plugin:HEAD-SNAPSHOT"
        // rest of classpath(s)…
    }
}

// module/build.gradle
```groovy
repositories {
    maven { url "https://oss.sonatype.org/content/repositories/snapshots" }
}

dependencies {
    def hiltVersion = "HEAD-SNAPSHOT"
    kapt "com.google.dagger:hilt-android-compiler:${hiltVersion}"
    implementation "com.google.dagger:hilt-android:${hiltVersion}"
}
```

## Key Features

* **Injecting state into ViewModels with AssistedInject**
  
  Since the `initialState` parameter is only available at runtime, Dagger can not provide this dependency for us. We need the [AssistedInject](https://github.com/square/AssistedInject) library for this purpose.

* **Multibinding setup for AssistedInject Factories**

  Every ViewModel using AssistedInject needs a Factory interface annotated with `@AssistedFactory`. These factories are grouped together under a common parent type [AssistedViewModelFactory](src/main/java/com/airbnb/mvrx/hellohilt/di/AssistedViewModelFactory.kt) to enable a Multibinding Dagger setup.

## Example

* Create your ViewModel with an ``@AssistedInject` constructor, an `@AssistedFactory` implementing `AssistedViewModelFactory`, and a companion object implementing `DaggerMvRxViewModelFactory`.

```kotlin
class MyViewModel @AssistedInject constructor(
  @Assisted initialState: MyState,
  // and other dependencies
) {

  @AssistedFactory
  interface Factory: AssistedViewModelFactory<MyViewModel, MyState> {
    override fun create(initialState: MyState): MyViewModel
  }

  companion object: HiltMavericksViewModelFactory<MyViewModel, MyState>(MyViewModel::class.java)
}
```

* Tell Dagger to include your ViewModel's AssistedInject Factory in a Multibinding map.

```kotlin
interface AppModule {

    @[Binds IntoMap ViewModelKey(MyViewModel::class)]
    fun myViewModelFactory(factory: MyViewModel.Factory): AssistedViewModelFactory<*, *>

}
```

* Add a provision for the Multibinding map in your Dagger component:

```kotlin
    fun viewModelFactories(): Map<Class<out MavericksViewModel<*>>, AssistedViewModelFactory<*, *>>
```

* With this setup complete, request your ViewModel in a Fragment as usual, using any of MvRx's ViewModel delegates.

```kotlin
class MyFragment : BaseMvRxFragment() {
  val viewModel: MyViewModel by fragmentViewModel()
}
```

## How it works

`HiltMavericksViewModelFactory` will try loading the custom entry point from the `SingletonComponent`
and lookup the viewModel factory using the `viewModelClass` key.
