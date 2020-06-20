# Threading

One challenging aspect of Android is that everything happens on the main thread by default. Interacting with views must be done there but many other things such as business logic are only there because it is too much work to do otherwise. One of the core tenants of Mavericks is that it is thread-safe. Everything non-view related in Mavericks can and does run on background threads. Mavericks abstracts away most of the challenges of multi-threading. However, it is important to be aware of this when using Mavericks.

State updates are not processed synchronously. They are placed on a queue and run on a background thread.
In other words:
```kotlin
fun setCount() {
  // Count is 0
  setState { copy(count = 1) }
  // Count is still 0 until the reducer above is run.
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

You can view some more complex ordering situations [here](https://github.com/airbnb/MvRx/blob/release/2.0.0/mvrx/src/test/kotlin/com/airbnb/mvrx/SetStateWithStateOrderingTest.kt). However, most of the time it will be have as you would expect and you don't have to think about the threading model.
