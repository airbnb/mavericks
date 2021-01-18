[![Build Status](https://travis-ci.com/airbnb/mavericks.svg?branch=master)](https://travis-ci.com/github/airbnb/mavericks)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.airbnb.android/mavericks/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.airbnb.android/mavericks)

# Mavericks

Mavericks is an Android MVI framework that is both easy to learn yet powerful enough for the most complex flows at [Airbnb](https://www.airbnb.com/), [Tonal](http://tonal.com/), and other large apps.

When we began creating Mavericks, our goal was to make building products easier, faster, and more fun. We believe that for Mavericks to be successful, it must be easy to learn for people new to Android development working their first apps yet powerful enough to support the most complex screens at Airbnb.

Mavericks is used in hundreds of screens at Airbnb including 100% of new screens. It has also been adopted by countless other apps from small smaple apps to apps with over 1 billion downloads.

Mavericks is built on top of [Android Jetpack](https://developer.android.com/jetpack) and [Kotlin Coroutines](https://developer.android.com/kotlin/coroutines) so it can be thought of as a complement rather than a departure from Google's standard set of libraries.

This is what a simple screen looks like. More complex screens may have more state properties or ViewModel functions but they rarely get much more complex to debug or hard to read.
```kotlin
/** State classes contain all of the data you need to render a screen. */
data class CounterState(val count: Int = 0) : MavericksState

/** ViewModels are where all of your business logic lives. It has a simple lifecycle and is easy to test. */
class CounterViewModel(initialState: CounterState) : MavericksViewModel<CounterState>(initialState) {
    fun incrementCount() = setState { copy(count = count + 1) }
}

/**
 * Fragments in Mavericks are simple and rarely do more than bind your state to views.
 * Mavericks works well with Fragments but you can use it with whatever view architecture you use.
 */
class CounterFragment : Fragment(R.layout.counter_fragment), MavericksView {
    private val viewModel: CounterViewModel by fragmentViewModel()

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

## Introduction to Mavericks (Droidcon Italy 2019)

This conference talk was designed for MvRx 1.0 but nearly all of the concepts still apply.

<iframe width="560" height="315" src="https://www.youtube.com/embed/Web4xPi2Ga4" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>
