# Threading

For the most part, you never have to think about or even be aware of the threading model Mavericks uses under the hood. However, it may be good to familiarize yourself with it to understanding what is happening under the hood.

Mavericks is thread-safe and nearly everything non-view related runs on background threads. However, Mavericks abstracts away most of the challenges of multi-threading.

State updates are not processed synchronously. They are placed on a queue and run on a background thread.
In other words:
```kotlin
fun setCount() {
  // Count is 0
  setState { copy(count = 1) }
  // Count is still 0 until the reducer above is run on the reducer queue thread.
}
```

To make this easier to work with, `withState` calls from within a ViewModel are run on the same thread after the `setState` queue has been fully flushed.
```kotlin
fun setAndRetrieveState() {
  println('A')
  setState {
    println('B')
    copy(count = 1)
  }
  println('C')
  withState { state ->
    println('D')
  }
  println('E')
}
```
This would print: [A, C, E, B, D]

You can view some more complex ordering situations [here](https://github.com/airbnb/mavericks/blob/release/2.0.0/mvrx/src/test/kotlin/com/airbnb/mvrx/SetStateWithStateOrderingTest.kt). However, most of the time it will be have as you would expect and you don't have to think about the threading model.

However, it is sometimes necessary to get data synchronously for views so as a convenience, `withState` _is_ run synchronously when called from outside of the ViewModel.
```kotlin
fun invalidate() = withState(viewModel) { state ->
    // This block is run immediately and returns the final expression of this lambda.
}
```

By default Mavericks creates a shared dispatcher for processing `withState`/`setState` queue. You can set your own dispatcher (e.g. `Dispatchers.Default`) by injecting it into `MavericksViewModelConfigFactory.storeContextOverride`