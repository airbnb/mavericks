package com.airbnb.mvrx

import androidx.annotation.CallSuper
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

/**
 * All Mavericks ViewModels must extend this class. In Mavericks, ViewModels are generic on a single state class. The ViewModel owns all
 * state modifications via [setState] and other classes may observe the state.
 *
 * From a [MavericksView]/Fragment, using the view model provider delegates will automatically subscribe to state updates in a lifecycle-aware way
 * and call [MavericksView.invalidate] whenever it changes.
 *
 * Other classes can observe the state via [stateFlow].
 */
abstract class MavericksViewModel<S : MavericksState>(
    initialState: S,
    configFactory: MavericksViewModelConfigFactory = Mavericks.viewModelConfigFactory,
) : MavericksRepository<S>(
    initialState,
    object : MavericksRepositoryConfigProvider {
        override fun <S : MavericksState> invoke(repository: MavericksRepository<S>, initialState: S): MavericksRepositoryConfig<S> {
            return configFactory.provideConfig(repository as MavericksViewModel<S>, initialState)
        }
    }
) {

    private val lastDeliveredStates = ConcurrentHashMap<String, Any?>()
    private val activeSubscriptions = Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())

    @CallSuper
    open fun onCleared() {
        coroutineScope.cancel()
    }

    @InternalMavericksApi
    override fun validateState(initialState: S) {
        super.validateState(initialState)

        // Assert that state can be saved and restored.
        val bundle = persistMavericksState(state = state, validation = true)
        restorePersistedMavericksState(bundle, initialState, validation = true)
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    internal fun <T : Any> Flow<T>.resolveSubscription(
        lifecycleOwner: LifecycleOwner?,
        deliveryMode: DeliveryMode,
        action: suspend (T) -> Unit
    ): Job {
        return if (lifecycleOwner != null) {
            collectLatest(lifecycleOwner, lastDeliveredStates, activeSubscriptions, deliveryMode, action)
        } else {
            resolveSubscription(action)
        }
    }

    override fun <S : MavericksState> onExecute(repository: MavericksRepository<S>): MavericksBlockExecutions {
        return (config as MavericksViewModelConfig).onExecute(repository as MavericksViewModel<S>)
    }
}
