package com.airbnb.mvrx

import androidx.annotation.CallSuper
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KProperty1

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
) {
    // Use the same factory for the life of the viewmodel, as it might change after this viewmodel is created (especially during tests)
    @PublishedApi
    internal val configFactory = Mavericks.viewModelConfigFactory

    @Suppress("LeakingThis")
    @InternalMavericksApi
    val config: MavericksViewModelConfig<S> = configFactory.provideConfig(
        this,
        initialState
    )

    val viewModelScope = config.coroutineScope

    private val repository = Repository()
    private val lastDeliveredStates = ConcurrentHashMap<String, Any?>()
    private val activeSubscriptions = Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())

    /**
     * Synchronous access to state is not exposed externally because there is no guarantee that
     * all setState reducers have run yet.
     */
    internal val state: S
        get() = repository.state

    /**
     * Return the current state as a Flow. For certain situations, this may be more convenient
     * than subscribe and selectSubscribe because it can easily be composed with other
     * coroutines operations and chained with operators.
     *
     * This WILL emit the current state followed by all subsequent state updates.
     *
     * This is not a StateFlow to prevent the ViewModel from having synchronous access to state. withState { state -> } should
     * be used as it is guaranteed to be run after all pending setState reducers have run.
     */
    val stateFlow: Flow<S>
        get() = repository.stateFlow

    init {
        if (config.debugMode) {
            viewModelScope.launch(Dispatchers.Default) {
                validateState(initialState)
            }
        }
    }

    @CallSuper
    open fun onCleared() {
        viewModelScope.cancel()
    }

    /**
     * Validates a number of properties on the state class. This cannot be called from the main thread because it does
     * a fair amount of reflection.
     */
    private fun validateState(initialState: S) {
        // Assert that state can be saved and restored.
        val bundle = persistMavericksState(state = state, validation = true)
        restorePersistedMavericksState(bundle, initialState, validation = true)
    }

    /**
     * Call this to mutate the current state.
     * A few important notes about the state reducer.
     * 1) It will not be called synchronously or on the same thread. This is for performance and accuracy reasons.
     * 2) Similar to the execute lambda above, the current state is the state receiver so the `count` in `count + 1` is actually the count
     *    property of the state at the time that the lambda is called.
     * 3) In development, Mavericks will do checks to make sure that your setState is pure by calling in multiple times. As a result, DO NOT use
     *    mutable variables or properties from outside the lambda or else it may crash.
     */
    protected fun setState(reducer: S.() -> S) {
        repository.setStateInternal(reducer)
    }

    /**
     * Calling this function suspends until all pending setState reducers are run and then returns the latest state.
     * As a result, it is safe to call setState { } and assume that the result from a subsequent awaitState() call will have that state.
     */
    suspend fun awaitState(): S {
        return repository.awaitState()
    }

    /**
     * Access the current ViewModel state. Takes a block of code that will be run after all current pending state
     * updates are processed.
     */
    protected fun withState(action: (state: S) -> Unit) {
        repository.withStateInternal(action)
    }

    /**
     * Run a coroutine and wrap its progression with [Async] property reduced to the global state.
     *
     * @param dispatcher A custom coroutine dispatcher that the coroutine will run on. If null, uses the dispatcher in [viewModelScope],
     *                  which defaults to [Dispatchers.Main.immediate] and can be overridden globally with [Mavericks.initialize].
     * @param retainValue A state property that, when set, will be called to retrieve an optional existing data value that will be retained across
     *                    subsequent Loading and Fail states. This is useful if you want to display the previously successful data when
     *                    refreshing.
     * @param reducer A reducer that is applied to the current state and should return the new state. Because the state is the receiver
     *                and is likely a data class, an implementation may look like: `{ copy(response = it) }`.
     */
    protected open fun <T : Any?> Deferred<T>.execute(
        dispatcher: CoroutineDispatcher? = null,
        retainValue: KProperty1<S, Async<T>>? = null,
        reducer: S.(Async<T>) -> S
    ) = suspend { await() }.execute(dispatcher, retainValue, reducer)

    /**
     * Run a coroutine and wrap its progression with [Async] property reduced to the global state.
     *
     * @param dispatcher A custom coroutine dispatcher that the coroutine will run on. If null, uses the dispatcher in [viewModelScope],
     *                  which defaults to [Dispatchers.Main.immediate] and can be overridden globally with [Mavericks.initialize].
     * @param retainValue A state property that, when set, will be called to retrieve an optional existing data value that will be retained across
     *                    subsequent Loading and Fail states. This is useful if you want to display the previously successful data when
     *                    refreshing.
     * @param reducer A reducer that is applied to the current state and should return the new state. Because the state is the receiver
     *                and is likely a data class, an implementation may look like: `{ copy(response = it) }`.
     */
    protected open fun <T : Any?> (suspend () -> T).execute(
        dispatcher: CoroutineDispatcher? = null,
        retainValue: KProperty1<S, Async<T>>? = null,
        reducer: S.(Async<T>) -> S
    ): Job {
        return with(repository) {
            executeInternal(dispatcher, retainValue, reducer)
        }
    }

    /**
     * Collect a Flow and wrap its progression with [Async] property reduced to the global state.
     *
     * @param dispatcher A custom coroutine dispatcher that the coroutine will run on. If null, uses the dispatcher in [viewModelScope],
     *                  which defaults to [Dispatchers.Main.immediate] and can be overridden globally with [Mavericks.initialize].
     * @param retainValue A state property that, when set, will be called to retrieve an optional existing data value that will be retained across
     *                    subsequent Loading and Fail states. This is useful if you want to display the previously successful data when
     *                    refreshing.
     * @param reducer A reducer that is applied to the current state and should return the new state. Because the state is the receiver
     *                and is likely a data class, an implementation may look like: `{ copy(response = it) }`.
     */
    protected open fun <T> Flow<T>.execute(
        dispatcher: CoroutineDispatcher? = null,
        retainValue: KProperty1<S, Async<T>>? = null,
        reducer: S.(Async<T>) -> S
    ): Job {
        return with(repository) {
            executeInternal(dispatcher, retainValue, reducer)
        }
    }

    /**
     * Collect a Flow and update state each time it emits a value. This is functionally the same as wrapping onEach with a setState call.
     *
     * @param dispatcher A custom coroutine dispatcher that the coroutine will run on. If null, uses the dispatcher in [viewModelScope],
     *                  which defaults to [Dispatchers.Main.immediate] and can be overridden globally with [Mavericks.initialize].
     * @param reducer A reducer that is applied to the current state and should return the new state. Because the state is the receiver
     *                and is likely a data class, an implementation may look like: `{ copy(response = it) }`.
     */
    protected open fun <T> Flow<T>.setOnEach(
        dispatcher: CoroutineDispatcher? = null,
        reducer: S.(T) -> S
    ): Job {
        return with(repository) {
            setOnEachInternal(dispatcher, reducer)
        }
    }

    /**
     * Subscribe to all state changes.
     *
     * @param action supports cooperative cancellation. The previous action will be cancelled if it is not completed before
     * the next one is emitted.
     */
    protected fun onEach(
        action: suspend (S) -> Unit
    ) = repository._internal(action)

    /**
     * Subscribe to state changes for a single property.
     *
     * @param action supports cooperative cancellation. The previous action will be cancelled if it is not completed before
     * the next one is emitted.
     */
    protected fun <A> onEach(
        prop1: KProperty1<S, A>,
        action: suspend (A) -> Unit
    ) = repository._internal1(prop1, action = action)

    /**
     * Subscribe to state changes for two properties.
     *
     * @param action supports cooperative cancellation. The previous action will be cancelled if it is not completed before
     * the next one is emitted.
     */
    protected fun <A, B> onEach(
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        action: suspend (A, B) -> Unit
    ) = repository._internal2(prop1, prop2, action = action)

    /**
     * Subscribe to state changes for three properties.
     *
     * @param action supports cooperative cancellation. The previous action will be cancelled if it is not completed before
     * the next one is emitted.
     */
    protected fun <A, B, C> onEach(
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        action: suspend (A, B, C) -> Unit
    ) = repository._internal3(prop1, prop2, prop3, action = action)

    /**
     * Subscribe to state changes for four properties.
     *
     * @param action supports cooperative cancellation. The previous action will be cancelled if it is not completed before
     * the next one is emitted.
     */
    protected fun <A, B, C, D> onEach(
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        prop4: KProperty1<S, D>,
        action: suspend (A, B, C, D) -> Unit
    ) = repository._internal4(prop1, prop2, prop3, prop4, action = action)

    /**
     * Subscribe to state changes for five properties.
     *
     * @param action supports cooperative cancellation. The previous action will be cancelled if it is not completed before
     * the next one is emitted.
     */
    protected fun <A, B, C, D, E> onEach(
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        prop4: KProperty1<S, D>,
        prop5: KProperty1<S, E>,
        action: suspend (A, B, C, D, E) -> Unit
    ) = repository._internal5(prop1, prop2, prop3, prop4, prop5, action = action)

    /**
     * Subscribe to state changes for six properties.
     *
     * @param action supports cooperative cancellation. The previous action will be cancelled if it is not completed before
     * the next one is emitted.
     */
    protected fun <A, B, C, D, E, F> onEach(
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        prop4: KProperty1<S, D>,
        prop5: KProperty1<S, E>,
        prop6: KProperty1<S, F>,
        action: suspend (A, B, C, D, E, F) -> Unit
    ) = repository._internal6(prop1, prop2, prop3, prop4, prop5, prop6, action = action)

    /**
     * Subscribe to state changes for seven properties.
     *
     * @param action supports cooperative cancellation. The previous action will be cancelled if it is not completed before
     * the next one is emitted.
     */
    protected fun <A, B, C, D, E, F, G> onEach(
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        prop4: KProperty1<S, D>,
        prop5: KProperty1<S, E>,
        prop6: KProperty1<S, F>,
        prop7: KProperty1<S, G>,
        action: suspend (A, B, C, D, E, F, G) -> Unit
    ) = repository._internal7(prop1, prop2, prop3, prop4, prop5, prop6, prop7, action = action)

    /**
     * Subscribe to changes in an async property. There are optional parameters for onSuccess
     * and onFail which automatically unwrap the value or error.
     *
     * @param onFail supports cooperative cancellation. The previous action will be cancelled if it as not completed before
     * the next one is emitted.
     * @param onSuccess supports cooperative cancellation. The previous action will be cancelled if it as not completed before
     * the next one is emitted.
     */
    protected fun <T> onAsync(
        asyncProp: KProperty1<S, Async<T>>,
        onFail: (suspend (Throwable) -> Unit)? = null,
        onSuccess: (suspend (T) -> Unit)? = null
    ) = repository._internalSF(asyncProp, onFail, onSuccess)

    @Suppress("EXPERIMENTAL_API_USAGE")
    internal fun <T : Any> Flow<T>.resolveSubscription(
        lifecycleOwner: LifecycleOwner? = null,
        deliveryMode: DeliveryMode,
        action: suspend (T) -> Unit
    ): Job {
        return if (lifecycleOwner != null) {
            collectLatest(lifecycleOwner, lastDeliveredStates, activeSubscriptions, deliveryMode, action)
        } else {
            with(repository) {
                resolveSubscription(action)
            }
        }
    }

    override fun toString(): String = "${this::class.java.name} $state"

    private inner class Repository : MavericksRepository<S>(
        MavericksRepositoryConfig(
            performCorrectnessValidations = config.debugMode,
            stateStore = config.stateStore,
            coroutineScope = config.coroutineScope,
            subscriptionCoroutineContextOverride = config.subscriptionCoroutineContextOverride,
            onExecute = { config.onExecute(this@MavericksViewModel) },
        )
    ) {
        fun setStateInternal(reducer: S.() -> S) {
            setState(reducer)
        }

        fun withStateInternal(action: (state: S) -> Unit) {
            withState(action)
        }

        fun <T : Any?> (suspend () -> T).executeInternal(
            dispatcher: CoroutineDispatcher? = null,
            retainValue: KProperty1<S, Async<T>>? = null,
            reducer: S.(Async<T>) -> S
        ): Job {
            return execute(dispatcher, retainValue, reducer)
        }

        fun <T> Flow<T>.executeInternal(
            dispatcher: CoroutineDispatcher? = null,
            retainValue: KProperty1<S, Async<T>>? = null,
            reducer: S.(Async<T>) -> S
        ): Job {
            return execute(dispatcher, retainValue, reducer)
        }

        fun <T> Flow<T>.setOnEachInternal(
            dispatcher: CoroutineDispatcher? = null,
            reducer: S.(T) -> S
        ): Job {
            return setOnEach(dispatcher, reducer)
        }
    }
}
