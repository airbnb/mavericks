# Async&lt;T&gt;
Mavericks makes dealing with async requests like fetching from a network, database, or anything else asynchronous easy. Mavericks includes `Async<T>`. Async is a [sealed class](https://kotlinlang.org/docs/reference/sealed-classes.html) with four subclasses:

Here is an abridged form:
```kotlin
sealed class Async<out T>(private val value: T?) {

    open operator fun invoke(): T? = value

    object Uninitialized : Async<Nothing>(value = null)

    data class Loading<out T>(private val value: T? = null) : Async<T>(value = value)

    data class Success<out T>(private val value: T) : Async<T>(value = value) {
        override operator fun invoke(): T = value
    }

    data class Fail<out T>(val error: Throwable, private val value: T? = null) : Async<T>(value = value)
}
```

You can directly invoke an Async object and it will return either the value if it is `Success` or null otherwise.
For example:
```kotlin
val foo = Success(5)
println(foo()) // 5
```

### Using Async for asynchronous actions (network, db, etc.) with `execute`
`MavericksViewModel` ships an `execute(...)` extension on common asynchronous types such as `suspend () -> T`, `Flow<T>`, and `Deferred<T>`. The `mvrx-rxjava2` artifact has extensions for `Observable<T>`, `Single<T>`, and `Completable<T>`.

When you call `execute` on one of these types, it will begin executing it, immediately emit `Loading`, and then emit `Success` or `Fail` when it succeeds, emits a new value, or fails.

Mavericks will automatically dispose of the subscription in `onCleared` of the ViewModel so you never have to manage the lifecycle or unsubscribing.

For each event it emits, it will call the `reducer` which takes the current state and returns an updated state just like `setState`

Executing a network request looks like:
```kotlin
interface WeatherApi {
    suspend fun fetchTemperature(): Int
}

// Inside of a function in your ViewModel.
suspend {
    weatherApi.fetchTemperature()
}.execute { copy(currentTemperature = it) }
```

Or for a [Kotlin Flow](https://kotlinlang.org/docs/reference/coroutines/flow.html):
```kotlin
interface WeatherRepository {
    fun fetchTemperature(): Flow<Int>
}

// Inside of a function in your ViewModel.
weatherRepository.fetchTemperature().execute { copy(currentTemperature = it) }
```

In this case:
* `currentTemperature` is of type `Async<Int>` and originally set to `Uninitialized`
* After calling `execute`, `currentTemperature` is set to `Loading()`
* If the API call succeeds, `currentTemperature` is set to `Success(temp)`
* If the API call fails, `currentTemperature` is set to `Fail(e)`
* If the ViewModel is cleared before `fetchTemperature()` completes, the API request is cancelled

### Subscribing to Async properties
If your state property is `Async`, you can use `onAsync` instead of `onEach` to subscribe to state changes.

You use it like:
```kotlin
data class MyState(val name: Async<String>) : MavericksState
...
onAsync(MyState::name) { name ->
    // Called when name is Success and any time it changes.
}

// Or if you want to handle failures
onAsync(
    MyState::name,
    onFail = { e -> .... },
    onSuccess = { name -> ... }
)
```

### Executing on a different dispatcher
You may want to run your suspend function on a different dispatcher. To do that, pass a `Dispatcher` as the first parameter to `execute()`:
```kotlin
suspend {
    weatherApi.fetchTemperature()
}.execute(Dispatchers.IO) { copy(currentTemperature = it) }
```

### Retaining data across reloads with `retainValue`
You may have a model where you want to refresh data and show the last successful data in addition to the loading/failure state of the refresh. To do this, use the optional `retainValue` parameter for `execute` and MvRx will automatically persist the value stored in that property to subsequent `Loading` or `Fail` states.

```kotlin
suspend {
    weatherApi.fetchTemperature()
}.execute(retainValue = MyState::currentTemperature) { copy(currentTemperature = it) }
```
In the previous example, if you called `fetchData()` again when data is `Success(5)`, the subsequent values will be:
* `Loading(5)`
* `Success(6)`

Your UI can check for `data is Loading` to determine whether to show a loading indicator yet call `data()` to render the most recent value while the new data loads.
You can also use `viewModel.onAsync` with on `onFail` block to show a snackbar or error message when the refresh failed without having to take away the first set of data that you already displayed.