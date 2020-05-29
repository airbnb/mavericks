# Dependency Injection

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
