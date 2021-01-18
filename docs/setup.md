# Setting up Mavericks

Adding Mavericks to your app requires just three steps:

### 1. Add The Dependency

```groovy
dependencies {
  implementation 'com.airbnb.android:mavericks:x.y.z'
}
```
The latest version of Mavericks is [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.airbnb.android/mavericks/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.airbnb.android/mavericks)


### 2. Update Your Base Fragment/View
Make your base Fragment class implement `MavericksView`.
***

`MavericksView` is just an interface so you can compose it with your existing base Fragment.

`MavericksView` can be implemented on any `LifecycleOwner` so you may create your own view constructs if you don't want to use Fragments.

### 3. Initialize Mavericks :id=initialize

In your Application onCreate method, add a single line:
```kotlin
Mavericks.initialize(this)
```

If you plan to use [Mavericks Mocking](/mocking.md), use this line instead:
```kotlin
MockableMavericks.initialize(this)
```


If you plan on using Jetpack Navigation, you should also follow the setup steps at [here](/jetpack-navigation.md).

### [Optional] Set Custom Mavericks View Model Configuration

Mavericks lets you override some configuration that is used every time a new ViewModel is created. For example, you could change the default dispatcher for the state store or for subscriptions. Check out the docs for [ViewModelConfigFactory](https://github.com/airbnb/mavericks/blob/master/mavericks/src/main/kotlin/com/airbnb/mvrx/MavericksViewModelConfigFactory.kt) for more info.

**Note:** If you create your own config factory, ensure that you set `debugMode` correctly so that the [debug checks](https://github.com/airbnb/Mavericks/wiki#debug-checks) are run.


To use a custom config, add this parameter to your `Mavericks.initialize()` call:
```kotlin
viewModelConfigFactory = MavericksViewModelConfigFactory(applicationContext)
```
***
