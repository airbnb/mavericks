[![Build Status](https://travis-ci.com/airbnb/mavericks.svg?branch=master)](https://travis-ci.com/github/airbnb/mavericks)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.airbnb.android/mavericks/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.airbnb.android/mavericks)

# Mavericks (formerly MvRx): Android on Autopilot

## For full documentation, check out our [docs site](https://airbnb.io/mavericks).

Mavericks is the Android framework from Airbnb that we use for nearly all product development at Airbnb.

When we began creating Mavericks, our goal was not to create yet another architecture pattern for Airbnb, it was to make building products easier, faster, and more fun. All of our decisions have built on that. We believe that for Mavericks to be successful, it must be effective for building everything from the simplest of screens to the most complex in our app.

This is what it looks like:
```kotlin

data class HelloWorldState(val title: String = "Hello World") : MavericksState

/**
 * Refer to the wiki for how to set up your base ViewModel.
 */
class HelloWorldViewModel(initialState: HelloWorldState) : MavericksViewModel<HelloWorldState>(initialState) {
    fun getMoreExcited() = setState { copy(title = "$title!") }
}

class HelloWorldFragment : Fragment(R.layout.hello_world_fragment), MavericksView {
    private val viewModel: HelloWorldViewModel by fragmentViewModel()

    override fun invalidate = withState(viewModel) { state ->
        // Update your views with the latest state here.
        // This will get called any time your state changes and the viewLifecycleOwner is STARTED.
    }
}
```

## Installation

Gradle is the only supported build configuration, so just add the dependency to your project `build.gradle` file:

```groovy
dependencies {
  implementation 'com.airbnb.android:mavericks:x.y.z'
}
```
The latest version of mavericks is [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.airbnb.android/mavericks/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.airbnb.android/mavericks)

## For full documentation, check out the [docs site](https://airbnb.io/mavericks)

Legacy documentation for MvRx 1.x can still be found in the [wiki](https://github.com/airbnb/mavericks/wiki)
