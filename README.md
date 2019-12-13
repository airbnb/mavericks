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
class HelloWorldViewModel(initialState: HelloWorldState) : MyBaseMvRxViewModel<HelloWorldState>(initialState, debugMode = BuildConfig.DEBUG) {
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
  implementation 'com.airbnb.android:mvrx:x.y.z'
}
```
The latest version of mvrx is [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.airbnb.android/mvrx/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.airbnb.android/mvrx)

## Proguard/Dexguard/R8

The MvRx lib comes with a working configuration in general. However, app specific settings are often required. Have a look at the demo apps for one of those.

The configuration provided for the lib assumes that your shrinker can correctly handle Kotlin `@Metadata` annotations (e.g. Dexguard 8.6++) if it cannot (e.g. Proguard and current versions of R8) you need to add these rules to the proguard configuration of your app:

```
# BaseMvRxViewModels loads the Companion class via reflection and thus we need to make sure we keep
# the name of the Companion object.
-keepclassmembers class ** extends com.airbnb.mvrx.BaseMvRxViewModel {
    ** Companion;
}

# Members of the Kotlin data classes used as the state in MvRx are read via Kotlin reflection which cause trouble
# with Proguard if they are not kept.
# During reflection cache warming also the types are accessed via reflection. Need to keep them too.
-keepclassmembers,includedescriptorclasses,allowobfuscation class ** implements com.airbnb.mvrx.MvRxState {
   *;
}

# The MvRxState object and the names classes that implement the MvRxState interfrace need to be
# kept as they are accessed via reflection.
-keepnames class com.airbnb.mvrx.MvRxState
-keepnames class * implements com.airbnb.mvrx.MvRxState

# MvRxViewModelFactory is referenced via reflection using the Companion class name.
-keepnames class * implements com.airbnb.mvrx.MvRxViewModelFactory
```


## For full documentation, check out the [wiki](https://github.com/airbnb/MvRx/wiki)
