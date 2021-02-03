## Dagger

`MavericksViewModelFactory` can be combined with Dagger's [assisted injection](https://dagger.dev/dev-guide/assisted-injection) to provide seamless integration with Dagger.

For more information and working example, check out our [hellodagger](https://github.com/airbnb/MvRx/tree/master/hellodagger) example on GitHub.

```kotlin
class HelloDaggerViewModel @AssistedInject constructor(
    @Assisted state: HelloDaggerState,
    private val repo: HelloRepository
) : MavericksViewModel<HelloDaggerState>(state) {

    @AssistedFactory
    interface Factory : AssistedViewModelFactory<HelloDaggerViewModel, HelloDaggerState> {
        override fun create(state: HelloDaggerState): HelloDaggerViewModel
    }

    companion object : MavericksViewModelFactory<HelloDaggerViewModel, HelloDaggerState> by daggerMavericksViewModelFactory()
}
```


## Hilt

Mavericks also works great with Hilt. One difference from standard ViewModels is that instead of using the standard `ViewModelComponent` which is a subcomponent of `ActivityRetainedComponent`, you have to create your own custom component called `MavericksViewModelComponent` which is a child of `SingletonComponent`. However, this is functionally the same and you get all of the same benefits of a view model scoped component in which you can share dependencies between different components in the view model graph.

For more information and working example, check out our [hellohilt](https://github.com/airbnb/MvRx/tree/master/hellohilt) example on GitHub.

```kotlin
class HelloHiltViewModel @AssistedInject constructor(
    @Assisted state: HelloHiltState,
    private val repo1: HelloRepository,
) : BaseMvRxViewModel<HelloHiltState>(state) {

    @AssistedFactory
    interface Factory : AssistedViewModelFactory<HelloHiltViewModel, HelloHiltState> {
        override fun create(state: HelloHiltState): HelloHiltViewModel
    }

    companion object : MavericksViewModelFactory<HelloHiltViewModel, HelloHiltState> by hiltMavericksViewModelFactory()
}
```
