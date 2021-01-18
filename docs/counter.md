# Mavericks: Counter App

Getting started with Mavericks is simple. Once it is [integrated into your project](setup.md), let's walk through a simple counter example. We're going to make this:

![Counter](/images/counter.png)

When you click the counter text, the count goes up by one.
Code for this sample can be found [here](https://github.com/airbnb/mavericks/tree/master/counter).

## Create CounterState

The first step in creating a Mavericks screen is to model it as a function of state. Modeling a screen as a function of state is a useful concept because it is:
1. Easy to test
1. Easy for you and other engineers to reason through
1. Renders the same independently of the order of events leading up to it
1. Powerful enough to render any type of screen

A counter screen is simple to model.
```kotlin
data class CounterState(val count: Int = 0) : MavericksState
```
* CounterState contains a single immutable (`val`) property, count, it is an integer, and it defaults to 0.
* CounterState is a [data class](https://kotlinlang.org/docs/reference/data-classes.html) so that Kotlin creates equals and hashCode automatically.
* CounterState implementation of `MavericksState` signals the intention that this class will be used as Mavericks state.

## Create CounterViewModel

Next, we need to create a ViewModel which will own CounterState and be responsible for updating it.
```kotlin
class CounterViewModel(initialState: CounterState) : MavericksViewModel<CounterState>(state) {
    fun incrementCount() {
      setState { copy(count = count + 1) }
    }
}
```
* CounterViewModel takes CounterState as a constructor parameter because Mavericks creates your initial state for you. It does this to handle [saved state](/saved-state.md) across process restoration.
* incrementCount() calls setState which takes a reducer with the type `CounterState.() -> CounterState `. The receiver of the reducer is the current state when the reducer is run and it returns the updated state. `copy` is the kotlin data class copy function.

## Create CounterFragment

First, we need to create the layout. It is simply a TextView that will display the count in the middle of the screen.
```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    <TextView
        android:id="@+id/counterText"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:gravity="center"
        android:textSize="48dp" />
</FrameLayout>
```

Then, we need to create our Fragment.
```kotlin
class CounterFragment : Fragment(R.layout.counter_fragment), MavericksView {
    private val viewModel: CounterViewModel by fragmentViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        counterText.setOnClickListener {
            viewModel.incrementCount()
        }
    }

    override fun invalidate() = withState(viewModel) { state ->
        counterText.text = "${state.count}"
    }
}
```
* CounterFragment implements MavericksView but you may choose to have a base fragment that implements it instead.
* We use the Mavericks view model delegates to get or create a new ViewModel scoped to this Fragment. It will be created if one doesn't exist or return the existing one if it does, after rotating the screen, for example.
* We set up a click listener that calls `viewModel.incrementCount()`. Calling named functions on your ViewModel is like the _intent_ from the MVI world.
* We override `invalidate()` from `MavericksView`. `invalidate()` will be called any time the state for any view model retrieved with a view model delegate changes.
* We added `withState(viewModel)` to access the current state of our ViewModel at the time `invalidate()` was called.
* We render `state.count` to the `TextView`'s text.

***

Congratulations, you have built your first Mavericks screen!
