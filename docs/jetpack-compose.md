# Jetpack Compose

Mavericks is still useful in a Jetpack Compose world. With Jetpack Compose, you can use Compose for all of your UI but Mavericks for your business logic, data fetching, interface to your dependency injection object graph, etc.

To use Mavericks with Jetpack Compose, add the `com.airbnb.android:mavericks-compose` artifact (2.1.0-alpha01).

## Creating a MavericksViewModel

To get started, create a MavericksViewModel just like you always have. Now, with the `mavericks-compose` artifact, you can get or create a ViewModel in a composable like this:

```kotlin
@Composable
fun MyComponent() {
    val viewModel: CounterViewModel = mavericksViewModel()
    // You can now call functions on your viewModel.
}
```

By default, the view model will be scoped to the closest `LifecycleOwner` but you can specify a different scope if you need to. Refer to the docs for `mavericksViewModel()` for more information on custom scopes.

## Accessing State

If you call `collectAsState()` on your view model with no parameters, it will return a [Compose State property](https://developer.android.com/jetpack/compose/state) with the whole ViewModel state class and will update any time the state changes.

You can also pass in a state property reference or a mapper function to get a [Compose State property](https://developer.android.com/jetpack/compose/state) for changes to just that property or mapper function.

The recommended approach is to subscribe individually to a small set of State properties and to break down large Composables into smaller ones. This allows Compose to more efficiently recompose your UI when state changes.

```kotlin
@Composable
fun MyComponent() {
    val viewModel: CounterViewModel = mavericksViewModel()
    // All changes to your state
    val state by viewModel.collectAsState()
    // Just count.
    val count1 = viewModel.collectAsState(CounterState::count)
    // Equivalent to count but with more flexibility for nested props.
    val count2 = viewModel.collectAsState { it.count }

    Text("Count is ${state.count}")
    Text("Count is $count1")
    Text("Count is $count2")
}
```

## Compose + Mavericks Mental Model

When considering what type of code to put in Compose and what to put in Mavericks, consider:

1. Compose renders your screen as a function of State.
2. Mavericks creates and updates the state.

To use a concrete example, if you are fetching the weather, you would use Mavericks to fetch the weather for a specific zip code from an API using `execute`.

Your screen would then subscribe to that state and emit the appropriate UI using composables.

If the user wanted to look up the weather for another location, they would use the UI built with compose to type in a new location, and hit submit. That composable would then call a function on the MavericksViewModel to update the location which would then trigger a new weather API `execute` call.

The results of that call will automatically update in the composable subscribing to the weather state.

## Scoping ViewModels

By default, `mavericksViewModel()` will get or create a view model scoped to the nearest `LocalLifecycleOwner`. In most cases, this will be the closest `NavBackStackEntry`, `Fragment`, or `Activity`. If you need to specify a custom scope, pass in a different `LifecycleOwner` as the first parameter. For example, to scope a view model to the `Activity`, pass `LocalContext.current as ComponentActivity`. `ComponentActivity` is the super class of both `AppCompatActivity` and `FragmentActivity`.

As a convenience, `mavericksActivityViewModel()` exists to scope a ViewModel to the host activity.

Mavericks needs a `ViewModelStoreOwner` and `SavedStateRegistryOwner` as well. In many cases (such as `NavBackStackEntry`, `Fragment`, and `Activity`), the `LifecycleOwner` will also implement these classes but if you need to specify custom owners, you can. Just ensure that they have the same lifecycles or else you could get unexpected behavior.
