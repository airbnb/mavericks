# MvRx: Android on Autopilot

## For full documentation, check out the [wiki](https://github.com/airbnb/MvRx/wiki)

MvRx (pronounced mavericks) is the Android framework from Airbnb that we use for nearly all product development at Airbnb.

When we began creating MvRx, our goal was not to create yet another architecture pattern for Airbnb, it was to make building products easier, faster, and more fun. All of our decisions have built on that. We believe that for MvRx to be successful, it must be effective for building everything from the simplest of screens to the most complex in our app.

This is what it looks like:
```kotlin

data class HelloWorldState(val title: String = "Hello World") : MvRxState

/**
 * Refer to the wiki for how to set up your base ViewModel.
 */
class HelloWorldViewModel(initialState: HelloWorldState) : MyBaseMvRxViewModel<HelloWorldState>(initialState) {
    fun getMoreExcited() = setState { copy(title = "$title!") }
}

class HelloWorldFragment : BaseFragment() {
    private val viewModel: HelloWorldViewModel by fragmentViewModel()

    override fun EpoxyController.buildModels() = withState(viewModel) { state ->
        header {
            title(state.title)
        }
        basicRow { 
            onClick { viewModel.getMoreExcited() }
        }
    }
}
```

## Installation

Gradle is the only supported build configuration, so just add the dependency to your project `build.gradle` file:

```groovy
dependencies {
  implementation 'com.airbnb.android:mvrx:0.5.0'
}
```
The latest version of mvrx is [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.airbnb.android/mvrx/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.airbnb.android/mvrx)

## For full documentation, check out the [wiki](https://github.com/airbnb/MvRx/wiki)
