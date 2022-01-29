## Dagger

`MavericksViewModelFactory` can be combined with Dagger's [assisted injection](https://dagger.dev/dev-guide/assisted-injection) to provide seamless integration with Dagger.

For more information and working example, check out our [hellodagger](https://github.com/airbnb/MvRx/tree/main/hellodagger) example on GitHub.

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

Mavericks also works great with Hilt. One difference from standard ViewModels is that instead of using the standard `ViewModelComponent` which is a subcomponent of `ActivityRetainedComponent`, you have to use `MavericksViewModelComponent` which is a child of `SingletonComponent`. However, this is functionally the same and you get all of the same benefits of a view model scoped component in which you can share dependencies between different components in the view model graph.

For more information and working example, check out our [hellohilt](https://github.com/airbnb/MvRx/tree/main/hellohilt) example on GitHub.

```kotlin
import com.airbnb.mvrx.hilt.AssistedViewModelFactory
import com.airbnb.mvrx.hilt.hiltMavericksViewModelFactory

data class ExampleState(
    val data: String = "",
) : MavericksState

class ExampleViewModel @AssistedInject constructor(
    @Assisted initialState: ExampleState,
    private val exampleRepository: ExampleRepository,
) : MavericksViewModel<ExampleState>(initialState) {
    
    @AssistedFactory
    interface Factory : AssistedViewModelFactory<ExampleViewModel, ExampleState> {
        override fun create(state: ExampleState): ExampleViewModel
    }

    companion object : MavericksViewModelFactory<TestViewModel, TestState> by hiltMavericksViewModelFactory()
}
```
```kotlin
import com.airbnb.mvrx.hilt.AssistedViewModelFactory
import com.airbnb.mvrx.hilt.MavericksViewModelComponent

@Module
@InstallIn(MavericksViewModelComponent::class)
interface ExampleViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(ExampleViewModel::class)
    fun exampleViewModelFactory(factory: TestViewModel.Factory): AssistedViewModelFactory<*, *>
}
```
