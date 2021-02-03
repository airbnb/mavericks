## Core Concepts
Mastering Mavericks only requires using three classes: `MavericksState`, `MavericksViewModel`, and `MavericksView`.

## MavericksState
The first step in creating a Mavericks screen is to model it as a function of state. The MavericksState interface [doesn't do anything](https://github.com/airbnb/MvRx/blob/master/mavericks/src/main/kotlin/com/airbnb/mvrx/MavericksState.kt) itself but signals the intention of your class to be used as state.

Modeling a screen as a function of state is a useful concept because it is:
1. Thread safe
1. Easy for you and other engineers to reason through
1. Renders the same independently of the order of events leading up to it
1. Powerful enough to render any type of screen
1. Easy to test

Mavericks will also enforce that your state class:
1. Is a kotlin data class
1. Uses only immutable properties
1. Has default values for every property to ensure that your screen can be rendered immediately

Mavericks enforces these through its [debug checks](/debug-checks.md)

This concept makes reasoning about and testing a screen trivially easy because given a state class, you can have high confidence that your screen will look correct.
Example
```kotlin
data class UserState(
    val score: Int = 0,
    val previousHighScore: Int = 150,
    val livesLeft: Int = 99,
) : MavericksState
```

#### Derived Properties :id=derived
Because state is just a normal Kotlin data class, you can create derived properties to represent specific state conditions like this:
```kotlin
data class UserState(
    val score: Int = 0,
    val previousHighScore: Int = 150,
    val livesLeft: Int = 99,
) : MavericksState {
    // Properties inside the body of your state class are "derived".
    val pointsUntilHighScore = (previousHighScore - score).coerceAtLeast(0)
    val isHighScore = score >= previousHighScore
}
```
Placing logic as a derived property like `pointsUntilHighScore` or `isHighScore` means that:
1. You can subscribe to changes to just these derived properties using `onEach`.
1. It will _never_ be out of sync with the underlying state
1. It is extremely easy to reason about
1. It is extremely easy to write unit tests for

## MavericksViewModel

A ViewModel is responsible for:
1. Updating state
2. Exposing a stream of states for other classes to subscribe to (MavericksViewModel.stateFlow)

Mavericks ViewModels are conceptually nearly identical to [Jetpack ViewModels](https://developer.android.com/topic/libraries/architecture/viewmodel) with the addition of being generic on a MavericksState class.

### Updating state
From within a viewModel, you call `setState { copy(yourProp = newValue) }`. If this syntax is unfamiliar:
1. The signature of the lambda is `S.() -> S` meaning the receiver (aka `this`) of the lambda is the current state when the lambda is invoked and it returns the new lambda
1. `copy` comes from the fact that the state class is a Kotlin [data class](https://kotlinlang.org/docs/reference/data-classes.html)
1. The lambda is _not_ executed synchronously. It is put on a queue and run on a background thread. See [threading](threading.md) for more information

### Handling async/db/network operations
Handling asynchronous operations with ease was one of the primary goals of Mavericks. Check out [the docs](async.md) for `Async<T>` and `execute(...)` to learn more.

### Subscribing to state changes
You can subscribe to state changes in your ViewModel. You may want to do this for analytics, for example. This usually done in the `init { ... }` block.

```kotlin
// Invoked every time state changes
onEach { state ->
}
```

```kotlin
// Invoked whenever propA changes only.
onEach(YourState::propA) { a ->
}
// Invoked whenever propA, propB, or propC changes only.
onEach(YourState::propA, YourState::propB, YourState::propC) { a, b, c ->
}
```

**TIP:** If you are calling `setState` from within an `onEach` block, consider using a [derived property](#derived).

### stateFlow
`MavericksViewModel` exposes a `stateFlow` property which is a normal Kotlin Flow that emits the current state as well as any future updates and can be used however you would like. Helpers such as `onEach` above are just wrappers around it with automatic lifecycle cancellation.

### Accessing state once
If you just want to retrieve the value of state one time, you can use `withState { state -> ... }`.

When called from within a ViewModel, this will _not_ be run synchronously. It will be placed on a background queue so that all pending `setState` reducers are called prior to your `withState` call.

## MavericksView
`MavericksView` is where you actually render your state class to the screen. Most of the time, this will be a Fragment but it doesn't have to be.
By implementing `MavericksView`, you:
1. You can get access to a `MavericksViewModel` via any of the view model delegates. Doing so will automatically subscribe to changes and call `invalidate()`.
1. Override the `invalidate()` function. It is called any time the state for any view model accessed by the above delegates changes. `invalidate()` is used to redraw the UI on each state change

#### ViewModel delegates
1. `activityViewModel()` scopes the ViewModel to the Activity. All Fragments within the Activity that request a ViewModel of this type will receive the same instance. Useful for sharing data between screens.
1. `fragmentViewModel()` scopes the ViewModel to the Fragment. It will be accessible to children fragments but parent or sibling fragments would get a different instance.
1. `parentFragmentViewModel()` walks up the parent fragment tree until it finds one that has already created a ViewModel of the desired type. If none is found, it will automatically create one scoped to the highest parent fragment.
1. `existingViewModel()` Same as `activityViewModel()` except it throws if the ViewModel was not already created by somebody else.
1. `navGraphViewModel(navGraphId: Int)` scopes the ViewModel to the [Jetpack Navigation](https://developer.android.com/guide/navigation) graph with that id. This requires the mvrx-navigation artifact ([docs](jetpack-navigation.md)).

If you want multiple ViewModels of the same type, you can pass a `keyFactory` into any of the delegates.

#### Subscribing to state manually
Most of the time, overriding `invalidate()` and updating your views is sufficient. However, if you want to subscribe to state to do things like start animations, you may call any of the `onEach` subscriptions on your ViewModel. If your view is a Fragment, these subscriptions should be set up in `onCreate()`.

#### Accessing state once
If you just want to retrieve the value of state one time, you can use `withState { state -> ... }`.

When called from outside a ViewModel, this will _is_ be run synchronously.

#### Triggering state changes
The ViewModel should expose named functions that can be called by the view. For example, a counter view model could expose a function `incrementCount()` to create a clear API accessible to the view.
