# Jetpack Navigation

Mavericks ships with a `mavericks-navigation` artifact that makes it easy to use nav graph scoped ViewModels.

## Getting Started

Add the dependency to your build.gradle file:
```groovy
dependencies {
  implementation 'com.airbnb.android:mavericks-navigation:x.y.z'
}
```
The latest version of mavericks-navigation is [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.airbnb.android/mavericks-navigation/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.airbnb.android/mavericks-navigation)

## Initialize Mavericks

In addition to the other setup steps, when you [initialize Mavericks](/setup.md#initialize), you will need to pass in a custom viewModelDelegateFactory. To do that, replace:
```kotlin
Mavericks.initialize(this)
// or 
MockableMavericks.initialize(this)
```
with:
```kotlin
Mavericks.initialize(this, viewModelDelegateFactory = DefaultNavigationViewModelDelegateFactory())
// or 
MockableMavericks.initialize(this, viewModelDelegateFactory = DefaultNavigationViewModelDelegateFactory())
```

## Usage

Once you have done that, you may use the `navGraphViewModel()` delegate to get a ViewModel scoped to the navigation graph with that id.

To view an example of its usage, check out the sample [here](https://github.com/airbnb/mavericks/blob/master/sample-navigation/src/main/java/com/airbnb/mvrx/sample/navigation/FlowFragments.kt).
