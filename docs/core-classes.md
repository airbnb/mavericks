# Core Classes

# MaveircksViewModel

### Constructor Dependency Injection
MvRx supports adding dependencies via constructor injection. To leverage this, create a `companion object` in your ViewModel and have it implement ` MvRxViewModelFactory<YourViewModel, YourState>`. That will force you to implement a `create` function. It will pass in the `ViewModelContext` that contains a reference to the activity that hosts the fragment, and a reference to the fragment if the view model was created with a fragment scope. You should use the activity/fragment, if needed, to get or create your dagger/DI component and inject what you need. **DO NOT** pass a reference of your activity to your ViewModel. It will leak your Activity. Pass the application context if you must.

The code looks like this:
```kotlin
class MyViewModel(
    override val initialState: MyState,
    private val repository: MyRepository
) : MvRxViewModel<MyState>() {
    ...
    companion object : MvRxViewModelFactory<MyViewModel, MyState> {
        // This *must* be @JvmStatic for performance reasons.
        @JvmStatic
        override fun create(viewModelContext: ViewModelContext, state: MyState): MyViewModel {
            val repository: MyRepository by viewModelContext.activity.inject()
            return MyViewModel(state, repository)
        }
    }
}
```
And we use this pattern in the sample app [here](https://github.com/airbnb/MvRx/blob/master/sample/src/main/java/com/airbnb/mvrx/sample/features/dadjoke/DadJokeIndexFragment.kt).

### Subscribing to state manually.
You can subscribe to state changes in your ViewModel. You may want to do this for logging, for example. This usually done in the `init { ... }` block.
You can subscribe to state changes with:

```kotlin
subscribe { state -> }
```

```kotlin
selectSubscribe(YourState::propA) { a -> }
selectSubscribe(YourState::propA, YourState::propB, YourState::propC) { a, b, c -> }
```

```kotlin
asyncSubscribe(YourState::asyncProp, onFail = { error -> ... }) { successValue -> ... }
// or
asyncSubscribe(YourState::asyncProp) { successValue -> ... }
```

### Debugging State
Call `logStateChanges()` in the `init` of your ViewModel and if `debugMode` is set to true, it will log all state changes to logcat. Assuming your state class is a Kotlin data class, the toString() should be readable.

### Pagination
A successful pattern for pagination has been to have one state property store `Async<List<T>>` while another stores `List<T>`. The `Async` property contains the network request of the current page. This can be checked to prevent us from requesting duplicate pages simultaneously and can be queried to determine whether or not to show a loading indicator.
The reducer for the pagination request should append results to the results list if it is successful like:
```kotlin
MvRxReviewsRequest.create(1234, offset).execute {
    copy(reviews = reviews.appendAt(it()?.reviews, offset), reviewRequest = it)
}
```
MvRx also includes `appendAt` to replace everything after the specified offset with the results. It handles edge cases like ignoring appending null and handling index out of bounds issues.

### Dependent Requests
If you have an async request that is dependent on another, execute them like this:
```kotlin
/* In ViewModel */
init {
    asyncSubscribe(MyState::request1), onSuccess = {
        fetchRequest2()
    })
}
```

# MavericksState
##### Lists
You should use Kotlin's overloaded List operators to create new immutable lists like this:

```
setState { copy(yourList = yourList + it()) }
```
##### Maps
Use [custom copy and delete extension functions](https://gist.github.com/gpeal/3cf0907fc80094a833f9baa309a1f627) and treat them similar to data classes:

`setState { copy(yourMap = yourMap.copy(“a” to 1, “b” to 2)) }`

or

`setState { copy(yourMap = yourMap.delete(“a”, “b”)) }`


### Logging State Changes
You can log all state changes to logcat with `logStateChanges`

# MavericksView
### Subscribing to state manually.
You can call the same `subscribe`, `selectSubscribe`, and `asyncSubscribe` calls mentioned in the [ViewModel section](https://github.com/airbnb/MvRx/wiki/Advanced-Concepts#subscribing-to-state-manually). These subscribe calls are automatically lifecycle-aware so you never have to worry about them being called outside of the `STARTED` state.
