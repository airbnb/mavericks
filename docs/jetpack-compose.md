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
