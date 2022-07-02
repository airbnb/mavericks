# What's new in Mavericks 3.0

### Experimental MavericksRepository

Mavericks 3.0 introduces an experimental module `mvrx-core` with new abstraction `MavericksRepository` designed to provide a base class for any statefull repository implementation that owns and manages its state. This module is pure Kotlin and has no Android dependencies. Primary goal of this module is to provide the same API and behaviour as `MavericksViewModel` in Android modules.


### API Changes

Mavericks 3.0 introduces one breaking change: `MavericksViewModelConfig.BlockExecutions` is extracted into  `MavericksBlockExecutions`. In order to migrate, you need to update your code to use `MavericksBlockExecutions` instead of `MavericksViewModelConfig.BlockExecutions`.

### New Functionality

#### `MavericksRepository`

`MavericksRepository` behaves exactly like `MavericksViewModel` except it doesn't have any Android dependencies. Even more, under the hood `MavericksViewModel` uses `MavericksRepository` to manage its state. You can find that `MavericksRepository` and `MavericksViewModel` are very similar in terms of API and behaviour.
As this is experimental module you have to opt in to use it by `-Xopt-in=com.airbnb.mvrx.InternalMavericksApi` compilation argument.

```kotlin
data class Forecast(
    val temperature: Int,
    val precipitation: Int,
    val wind: Int,
)

data class WeatherForecastState(
    val forecasts: Async<List<Forecast>> = Uninitialized,
) : MavericksState

interface WeatherApi {
    suspend fun getForecasts(): List<Forecast>
}

class WeatherForecastRepository(
    scope: CoroutineScope,
    private val api: WeatherApi,
) : MavericksRepository<WeatherForecastState>(
    initialState = WeatherForecastState(),
    configProvider = { initialState ->
        MavericksRepositoryConfig(
            debugMode = true,
            stateStore = CoroutinesStateStore(initialState, scope),
            coroutineScope = scope,
        )
    },
) {
    init {
        suspend { api.getForecasts() }.execute { copy(forecasts = it) }
    }

    fun refresh() {
        suspend { api.getForecasts() }.execute { copy(forecasts = it) }
    }
}
```