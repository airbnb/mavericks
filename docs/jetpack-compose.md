# Jetpack Compose

Mavericks is still useful in a Jetpack Compose world. With Jetpack Compose, you can use Compose for all of your UI but Mavericks for your business logic, data fetching, interface to your dependency injection object graph, etc.

To use Mavericks with Jetpack Compose, add the `com.airbnb.android:mavericks-compose` artifact (TBD on versioning).

To get started, create a MavericksViewModel just like you always have. Now, with the `mavericks-compose` artifact, you can get or create a ViewModel in a composable like this:

```kotlin
@Composable
fun MyComponent() {
    val viewModel: CounterViewModel = mavericksViewModel()
    val count by viewModel.collectState(CounterState::count)
    Button(onClick = activityScopedViewModel::incrementCount) {
        Text("Count: $count")
    }
}
```

If you call `collectAsState()` with no parameters, it will return a Compose state property with the whole ViewModel state class and will update any time the state changes.

The recommended approach is to subscribe individually to a small set of State properties and to break down large Composables into smaller ones. This allows Compose to more efficiently recompose your UI when state changes.

## Compose + Mavericks Mental Model

When considering what type of code to put in Compose and what to put in Mavericks, consider:

1. Compose renders your screen as a function of State.
2. Mavericks creates and updates the state.

To use a concrete example, if you are fetching the weather, you would use Mavericks to fetch the weather for a specific zip code from an API using `execute`.

Your screen would then subscribe to that state and emit the appropriate UI using composables.

If the user wanted to look up the weather for another location, they would use the UI build with compose to type in a new location, and hit submit. That composable would then call a function on the MavericksViewModel to update the location which would then trigger a new weather API `execute` call.

The results of that call will automatically update in the composable subscribing to the weather state.
