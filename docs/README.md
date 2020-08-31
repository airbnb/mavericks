[![Build Status](https://travis-ci.com/airbnb/MvRx.svg?branch=master)](https://travis-ci.com/github/airbnb/MvRx)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.airbnb.android/mvrx/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.airbnb.android/mvrx)

# Mavericks

Mavericks is an Android MVI framework that is both easy to learn yet powerful enough for the most complex flows at Airbnb and other large apps.

When we began creating Mavericks, our goal was to make building products easier, faster, and more fun. We believe that for Mavericks to be successful, it must be easy to learn for people new to Android development working their first apps yet powerful enough to support the most complex screens at Airbnb.

Mavericks is used in hundreds of screens at Airbnb including 100% of new screens. It has also been adopted by countless other apps from small smaple apps to apps with over 1 billion downloads.

Mavericks is built on top of [Android Jetpack](https://developer.android.com/jetpack) so it can be thought of as a complement rather than a departure from Google's standard set of libraries.

This is what it looks like:
```kotlin
/** State classes contain all of the data you need to render a screen. */
data class CounterState(val count: Int = 0) : MavericksState

/** ViewModels are where all of your business logic lives. It has a simple lifecycle and is easy to test. */
class CounterViewModel(initialState: CounterState) : MavericksViewModel<CounterState>(initialState) {
    fun incrementCount() = setState { copy(count = count + 1) }
}

/** Fragments in MvRx are simple and rarely do more than bind your state to views. */
class CounterFragment : Fragment(R.layout.counter_fragment), MavericksView {
    private val viewModel: CounterViewModel by activityViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        counterText.setOnClickListener {
            viewModel.incrementCount()
        }
    }

    override fun invalidate() = withState(viewModel) { state ->
        counterText.text = "Count: ${state.count}"
    }
}
```

## Installation

```groovy
dependencies {
  implementation 'com.airbnb.android:mvrx:x.y.z'
}
```
The latest version of mvrx is [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.airbnb.android/mvrx/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.airbnb.android/mvrx)
