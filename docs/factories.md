# Factories
In many cases, it is sufficient to let Mavericks create your ViewModel and State instances. However, there are times when you want to intercept their creation to inject your own dependencies or initial state.

## MavericksViewModelFactory
To intercept either ViewModel or State creation, create a companion object in your ViewModel and make it implement `MavericksViewModelFactory`
```kotlin
class MyViewModel(initialState: MyState, dataStore: DataStore) : MavericksViewModel(initialState) {
    companion object : MavericksViewModelFactory<MyViewModel, MyState>
}
```

### Creating a State class
In your `MavericksViewModelFactory`, override `initialState(...)` and return the desired initial state. It will only be called once, when the ViewModel is first created. All subsequent state updates will be handled inside the ViewModel.

You can use this to bind things like the current userId or other state properties that can be set as initial values.

### Creating a ViewModel
In your `MavericksViewModelFactory`, override `create(...)` and return your ViewModel instance. You can use this to enable things like constructor injection in your ViewModels. See [dependency injection](dependency-injection.md) for more info.

The initial state passed in to this function should be passed directly into your ViewModel without further modification.

#### ViewModelContext
ViewModelContext will either be `ActivityViewModelContext` or `FragmentViewModelContext` depending on the scope of who owns the ViewModel. This is passed into both factory functions and can be used to do things like retrieve the application, activity, fragment, or fragment args. Any of those should be enough to do things like access your dependency injection graph.

## Example

```kotlin
class MyViewModel(
    initialState: MyState,
    ...
) : MavericksViewModel<MyState>(initialState) {
    ...

    companion object : MavericksViewModelFactory<MyViewModel, MyState> {

        override fun initialState(viewModelContext: ViewModelContext): MyState {
            return MyState(...)
        }

        override fun create(viewModelContext: ViewModelContext, state: MyState): MyViewModel {
            return MyViewModel(state, ...)
        }
    }
}

```
