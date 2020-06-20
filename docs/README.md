[![Build Status](https://travis-ci.com/airbnb/MvRx.svg?branch=master)](https://travis-ci.com/github/airbnb/MvRx)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.airbnb.android/mvrx/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.airbnb.android/mvrx)

# Mavericks

Mavericks is an Android MVI framework that is both easy to learn yet powerful enough for the most complex flows at Airbnb and other large apps.

When we began creating Mavericks, our goal was not to create yet another architecture pattern for Airbnb, it was to make building products easier, faster, and more fun. All of our decisions have built on that. We believe that for Mavericks to be successful, it must be effective for building everything from the simplest of screens to the most complex..

This is what it looks like:
```kotlin
/** State classes contain all of the data you need to render a screen. */
data class CounterState(@PersistState val count: Int = 0) : MvRxState

/** ViewModels are where all of your business logic lives. It has a simple lifecycle and is easy to test. */
class CounterViewModel(state: CounterState) : MavericksViewModel<CounterState>(state) {
    fun incrementCount() = setState { copy(count = count + 1) }
}

/** Fragments in MvRx are simple and rarely do more than bind your state to views. */
class CounterFragment : Fragment(R.layout.fragment_counter), MavericksView {
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
