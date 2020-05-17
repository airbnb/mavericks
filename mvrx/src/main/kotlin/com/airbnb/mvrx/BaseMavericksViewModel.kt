package com.airbnb.mvrx

import androidx.annotation.CallSuper
import androidx.annotation.RestrictTo
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KProperty1

/**
 * To use Mavericks, make a class MavericksViewModel or YourCompanyViewModel that extends BaseMavericksViewModel and set debugMode.
 *
 * Most of the time, debugMode should be set to `BuildConfig.DEBUG`. When debug mode is enabled, Mavericks will run a series of checks on
 * your state and reducers to ensure that you are using Mavericks safely and will catch numerous common errors and mistakes.
 *
 * Most base classes will look like:
 * ```
 * abstract class MavericksViewModel<S : MvRxState>(state: S) : BaseMavericksViewModel<S>(state, debugMode = BuildConfig.DEBUG)
 * ```
 */
abstract class BaseMavericksViewModel<S : MvRxState>(
    initialState: S,
    debugMode: Boolean,
    /**
     * Provide an overridden state store. This should only be used for tests and should only
     * be exposed via a shared base class within your app. If your features extend this
     * directly, do not override this in the primary constructor of your feature ViewModel.
     */
    stateStoreOverride: MvRxStateStore<S>? = null,
    /**
     * Provide a default context for viewModelScope. It will be added after [SupervisorJob]
     * and [Dispatchers.Main.immediate]. This should only be used for tests and should only
     * be exposed via a shared base class within your app. If your features extend this
     * directly, do not override this in the primary constructor of your feature ViewModel.
     */
    contextOverride: CoroutineContext? = null
) {
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    protected val debugMode = if (MvRxTestOverrides.FORCE_DEBUG == null) debugMode else MvRxTestOverrides.FORCE_DEBUG

    val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate + contextOverride)

    private val stateStore = stateStoreOverride ?: CoroutinesStateStore(initialState, viewModelScope)
    private val lastDeliveredStates = ConcurrentHashMap<String, Any>()
    private val activeSubscriptions = Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())

    private val tag by lazy { javaClass.simpleName }
    private val mutableStateChecker = if (debugMode) MutableStateChecker(initialState) else null

    /**
     * Synchronous access to state is not exposed externally because there is no guarantee that
     * all setState reducers have run yet.
     */
    internal val state: S
        get() = stateStore.state

    /**
     * Return the current state as a Flow. For certain situations, this may be more convenient
     * than subscribe and selectSubscribe because it can easily be composed with other
     * coroutines operations and chained with operators.
     *
     * This WILL emit the current state followed by all subsequent state updates.
     */
    val stateFlow: Flow<S>
        get() = stateStore.flow

    init {
        if (this.debugMode) {
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
        state::class.assertImmutability()
        // Assert that state can be saved and restored.
        val bundle = state.persistState(validation = true)
        bundle.restorePersistedState(initialState, validation = true)
    }

    /**
     * Call this to mutate the current state.
     * A few important notes about the state reducer.
     * 1) It will not be called synchronously or on the same thread. This is for performance and accuracy reasons.
     * 2) Similar to the execute lambda above, the current state is the state receiver so the `count` in `count + 1` is actually the count
     *    property of the state at the time that the lambda is called.
     * 3) In development, MvRx will do checks to make sure that your setState is pure by calling in multiple times. As a result, DO NOT use
     *    mutable variables or properties from outside the lambda or else it may crash.
     */
    protected fun setState(reducer: S.() -> S) {
        if (debugMode) {
            // Must use `set` to ensure the validated state is the same as the actual state used in reducer
            // Do not use `get` since `getState` queue has lower priority and the validated state would be the state after reduced
            stateStore.set {
                val firstState = this.reducer()
                val secondState = this.reducer()

                if (firstState != secondState) {
                    @Suppress("UNCHECKED_CAST")
                    val changedProp = firstState::class.java.declaredFields.asSequence()
                        .onEach { it.isAccessible = true }
                        .firstOrNull { property ->
                            @Suppress("Detekt.TooGenericExceptionCaught")
                            try {
                                property.get(firstState) != property.get(secondState)
                            } catch (e: Throwable) {
                                false
                            }
                        }
                    if (changedProp != null) {
                        throw IllegalArgumentException(
                            "Impure reducer set on ${this@BaseMavericksViewModel::class.java.simpleName}! " +
                                "${changedProp.name} changed from ${changedProp.get(firstState)} " +
                                "to ${changedProp.get(secondState)}. " +
                                "Ensure that your state properties properly implement hashCode."
                        )
                    } else {
                        throw IllegalArgumentException(
                            "Impure reducer set on ${this@BaseMavericksViewModel::class.java.simpleName}! Differing states were provided by the same reducer." +
                                "Ensure that your state properties properly implement hashCode. First state: $firstState -> Second state: $secondState"
                        )
                    }
                }
                mutableStateChecker?.onStateChanged(firstState)

                firstState
            }
        } else {
            stateStore.set(reducer)
        }
    }

    /**
     * Access the current ViewModel state. Takes a block of code that will be run after all current pending state
     * updates are processed.
     */
    protected fun withState(action: suspend (state: S) -> Unit) {
        stateStore.get { state ->
            viewModelScope.launch {
                action(state)
            }
        }
    }

    /**
     * Subscribe to state changes for only a single property.
     *
     * @param action supports cooperative cancellation. The previous action will be cancelled if it as not completed before
     * the next one is emitted.
     */
    protected fun onEach(
        action: suspend (S) -> Unit
    ) = onEachInternal(null, RedeliverOnStart, action)

    /**
     * Subscribe to state changes for a single property.
     *
     * @param action supports cooperative cancellation. The previous action will be cancelled if it as not completed before
     * the next one is emitted.
     */
    protected fun <A> onEach(
        prop1: KProperty1<S, A>,
        action: suspend (A) -> Unit
    ) = onEach1Internal(null, prop1, action = action)

    /**
     * Subscribe to state changes for two properties.
     *
     * @param action supports cooperative cancellation. The previous action will be cancelled if it as not completed before
     * the next one is emitted.
     */
    protected fun <A, B> onEach(
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        action: suspend (A, B) -> Unit
    ) = onEach2Internal(null, prop1, prop2, action = action)

    /**
     * Subscribe to state changes for three properties.
     *
     * @param action supports cooperative cancellation. The previous action will be cancelled if it as not completed before
     * the next one is emitted.
     */
    protected fun <A, B, C> onEach(
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        action: suspend (A, B, C) -> Unit
    ) = onEach3Internal(null, prop1, prop2, prop3, action = action)

    /**
     * Subscribe to state changes for four properties.
     *
     * @param action supports cooperative cancellation. The previous action will be cancelled if it as not completed before
     * the next one is emitted.
     */
    protected fun <A, B, C, D> onEach(
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        prop4: KProperty1<S, D>,
        action: suspend (A, B, C, D) -> Unit
    ) = onEach4Internal(null, prop1, prop2, prop3, prop4, action = action)

    /**
     * Subscribe to state changes for five properties.
     *
     * @param action supports cooperative cancellation. The previous action will be cancelled if it as not completed before
     * the next one is emitted.
     */
    protected fun <A, B, C, D, E> onEach(
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        prop4: KProperty1<S, D>,
        prop5: KProperty1<S, E>,
        action: suspend (A, B, C, D, E) -> Unit
    ) = onEach5Internal(null, prop1, prop2, prop3, prop4, prop5, action = action)

    /**
     * Subscribe to state changes for six properties.
     *
     * @param action supports cooperative cancellation. The previous action will be cancelled if it as not completed before
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
    ) = onEach6Internal(null, prop1, prop2, prop3, prop4, prop5, prop6, action = action)

    /**
     * Subscribe to state changes for seven properties.
     *
     * @param action supports cooperative cancellation. The previous action will be cancelled if it as not completed before
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
    ) = onEach7Internal(null, prop1, prop2, prop3, prop4, prop5, prop6, prop7, action = action)

    /**
     * Subscribe to changes in an async property. There are optional parameters for onSuccess
     * and onFail which automatically unwrap the value or error.
     *
     * @param action supports cooperative cancellation. The previous action will be cancelled if it as not completed before
     * the next one is emitted.
     */
    protected fun <T> onAsync(
        asyncProp: KProperty1<S, Async<T>>,
        onFail: (suspend (Throwable) -> Unit)? = null,
        onSuccess: (suspend (T) -> Unit)? = null
    ) = onAsyncInternal(null, asyncProp, RedeliverOnStart, onFail, onSuccess)

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun onEachInternal(
        owner: LifecycleOwner?,
        deliveryMode: DeliveryMode = RedeliverOnStart,
        action: suspend (S) -> Unit
    ) = stateFlow.resolveSubscription(owner, deliveryMode, action)

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun <A> onEach1Internal(
        owner: LifecycleOwner?,
        prop1: KProperty1<S, A>,
        deliveryMode: DeliveryMode = RedeliverOnStart,
        action: suspend (A) -> Unit
    ) = stateFlow
        .map { MvRxTuple1(prop1.get(it)) }
        .distinctUntilChanged()
        .resolveSubscription(owner, deliveryMode.appendPropertiesToId(prop1)) { (a) ->
            action(a)
        }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun <A, B> onEach2Internal(
        owner: LifecycleOwner?,
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        deliveryMode: DeliveryMode = RedeliverOnStart,
        action: suspend (A, B) -> Unit
    ) = stateFlow
        .map { MvRxTuple2(prop1.get(it), prop2.get(it)) }
        .distinctUntilChanged()
        .resolveSubscription(owner, deliveryMode.appendPropertiesToId(prop1, prop2)) { (a, b) ->
            action(a, b)
        }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun <A, B, C> onEach3Internal(
        owner: LifecycleOwner?,
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        deliveryMode: DeliveryMode = RedeliverOnStart,
        action: suspend (A, B, C) -> Unit
    ) = stateFlow
        .map { MvRxTuple3(prop1.get(it), prop2.get(it), prop3.get(it)) }
        .distinctUntilChanged()
        .resolveSubscription(owner, deliveryMode.appendPropertiesToId(prop1, prop2, prop3)) { (a, b, c) ->
            action(a, b, c)
        }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun <A, B, C, D> onEach4Internal(
        owner: LifecycleOwner?,
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        prop4: KProperty1<S, D>,
        deliveryMode: DeliveryMode = RedeliverOnStart,
        action: suspend (A, B, C, D) -> Unit
    ) = stateFlow
        .map { MvRxTuple4(prop1.get(it), prop2.get(it), prop3.get(it), prop4.get(it)) }
        .distinctUntilChanged()
        .resolveSubscription(owner, deliveryMode.appendPropertiesToId(prop1, prop2, prop3, prop4)) { (a, b, c, d) ->
            action(a, b, c, d)
        }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun <A, B, C, D, E> onEach5Internal(
        owner: LifecycleOwner?,
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        prop4: KProperty1<S, D>,
        prop5: KProperty1<S, E>,
        deliveryMode: DeliveryMode = RedeliverOnStart,
        action: suspend (A, B, C, D, E) -> Unit
    ) = stateFlow
        .map { MvRxTuple5(prop1.get(it), prop2.get(it), prop3.get(it), prop4.get(it), prop5.get(it)) }
        .distinctUntilChanged()
        .resolveSubscription(owner, deliveryMode.appendPropertiesToId(prop1, prop2, prop3, prop4, prop5)) { (a, b, c, d, e) ->
            action(a, b, c, d, e)
        }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun <A, B, C, D, E, F> onEach6Internal(
        owner: LifecycleOwner?,
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        prop4: KProperty1<S, D>,
        prop5: KProperty1<S, E>,
        prop6: KProperty1<S, F>,
        deliveryMode: DeliveryMode = RedeliverOnStart,
        action: suspend (A, B, C, D, E, F) -> Unit
    ) = stateFlow
        .map { MvRxTuple6(prop1.get(it), prop2.get(it), prop3.get(it), prop4.get(it), prop5.get(it), prop6.get(it)) }
        .distinctUntilChanged()
        .resolveSubscription(owner, deliveryMode.appendPropertiesToId(prop1, prop2, prop3, prop4, prop5, prop6)) { (a, b, c, d, e, f) ->
            action(a, b, c, d, e, f)
        }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun <A, B, C, D, E, F, G> onEach7Internal(
        owner: LifecycleOwner?,
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        prop4: KProperty1<S, D>,
        prop5: KProperty1<S, E>,
        prop6: KProperty1<S, F>,
        prop7: KProperty1<S, G>,
        deliveryMode: DeliveryMode = RedeliverOnStart,
        action: suspend (A, B, C, D, E, F, G) -> Unit
    ) = stateFlow
        .map { MvRxTuple7(prop1.get(it), prop2.get(it), prop3.get(it), prop4.get(it), prop5.get(it), prop6.get(it), prop7.get(it)) }
        .distinctUntilChanged()
        .resolveSubscription(owner, deliveryMode.appendPropertiesToId(prop1, prop2, prop3, prop4, prop5, prop6, prop7)) { (a, b, c, d, e, f, g) ->
            action(a, b, c, d, e, f, g)
        }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun <T> onAsyncInternal(
        owner: LifecycleOwner?,
        asyncProp: KProperty1<S, Async<T>>,
        deliveryMode: DeliveryMode = RedeliverOnStart,
        onFail: (suspend (Throwable) -> Unit)? = null,
        onSuccess: (suspend (T) -> Unit)? = null
    ) = onEach1Internal(owner, asyncProp, deliveryMode.appendPropertiesToId(asyncProp)) { asyncValue ->
        if (onSuccess != null && asyncValue is Success) {
            onSuccess(asyncValue())
        } else if (onFail != null && asyncValue is Fail) {
            onFail(asyncValue.error)
        }
    }

    private fun <T : Any> Flow<T>.resolveSubscription(
        lifecycleOwner: LifecycleOwner? = null,
        deliveryMode: DeliveryMode,
        action: suspend (T) -> Unit
    ): Job {
        val flow = if (lifecycleOwner == null || MvRxTestOverrides.FORCE_DISABLE_LIFECYCLE_AWARE_OBSERVER) {
            this
        } else if (deliveryMode is UniqueOnly) {
            val lastDeliveredValue: T? = lastDeliveredValue(deliveryMode)
            this
                .assertOneActiveSubscription(lifecycleOwner, deliveryMode)
                .dropWhile { it == lastDeliveredValue }
                .flowWhenStarted(lifecycleOwner)
                .distinctUntilChanged()
                .onEach { lastDeliveredStates[deliveryMode.subscriptionId] = it }
        } else {
            flowWhenStarted(lifecycleOwner)
        }
        val scope = lifecycleOwner?.lifecycleScope ?: viewModelScope
        return scope.launch {
            flow.collectLatest { action(it) }
        }.cancelOnClear()
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    private fun <T> Flow<T>.assertOneActiveSubscription(owner: LifecycleOwner, deliveryMode: UniqueOnly): Flow<T> {
        val observer = object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                if (activeSubscriptions.contains(deliveryMode.subscriptionId)) error(duplicateSubscriptionMessage(deliveryMode))
                activeSubscriptions += deliveryMode.subscriptionId
            }

            override fun onDestroy(owner: LifecycleOwner) {
                activeSubscriptions.remove(deliveryMode.subscriptionId)
            }
        }

        owner.lifecycle.addObserver(observer)
        return onCompletion {
            activeSubscriptions.remove(deliveryMode.subscriptionId)
            owner.lifecycle.removeObserver(observer)
        }
    }

    private fun <T> lastDeliveredValue(deliveryMode: UniqueOnly): T? {
        @Suppress("UNCHECKED_CAST")
        return lastDeliveredStates[deliveryMode.subscriptionId] as T?
    }

    private fun duplicateSubscriptionMessage(deliveryMode: UniqueOnly) = """
        Subscribing with a duplicate subscription id: ${deliveryMode.subscriptionId}.
        If you have multiple uniqueOnly subscriptions in a MvRx view that listen to the same properties
        you must use a custom subscription id. If you are using a custom MvRxView, make sure you are using the proper
        lifecycle owner. See BaseMvRxFragment for an example.
    """.trimIndent()

    private operator fun CoroutineContext.plus(other: CoroutineContext?) = if (other == null) this else this + other

    private fun <S : MvRxState> assertSubscribeToDifferentViewModel(viewModel: BaseMavericksViewModel<S>) {
        require(this != viewModel) {
            "This method is for subscribing to other view models. Please pass a different instance as the argument."
        }
    }

    private fun Job.cancelOnClear(): Job {
        viewModelScope.coroutineContext[Job]?.invokeOnCompletion {
            cancel()
        }
        return this
    }

    override fun toString(): String = "${this::class.java.simpleName} $state"
}
