package com.airbnb.mvrx

import android.util.Log
import androidx.annotation.CallSuper
import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.airbnb.mvrx.MvRxTestOverrides.FORCE_DISABLE_LIFECYCLE_AWARE_OBSERVER
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

/**
 * To use MvRx, create your own base MvRxViewModel that extends this one and sets debugMode.
 *
 * All subsequent ViewModels in your app should use that one.
 */
abstract class BaseMvRxViewModel<S : MvRxState>(
    initialState: S,
    debugMode: Boolean,
    private val stateStore: MvRxStateStore<S> = RealMvRxStateStore(initialState)
) : ViewModel() {
    private val debugMode = if (MvRxTestOverrides.FORCE_DEBUG == null) debugMode else MvRxTestOverrides.FORCE_DEBUG

    private val tag by lazy { javaClass.simpleName }
    private val disposables = CompositeDisposable()
    private lateinit var mutableStateChecker: MutableStateChecker<S>
    private val lastDeliveredStates = ConcurrentHashMap<String, Any>()
    private val activeSubscriptions = Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())

    internal val state: S
        get() = stateStore.state

    init {
        Completable.fromCallable { warmReflectionCache(initialState) }.subscribeOn(Schedulers.computation()).subscribe()

        if (this.debugMode) {
            mutableStateChecker = MutableStateChecker(initialState)

            Completable.fromCallable { validateState(initialState) }
                .subscribeOn(Schedulers.computation()).subscribe()
        }
    }

    /**
     * Kotlin reflection has a large overhead the first time you run it
     * but then is pretty fast on subsequent times. Running these methods now will
     * initialize kotlin reflect and warm the cache so that when persistState() gets
     * called synchronously in onSaveInstanceState() on the main thread, it will be much faster.
     * This improved performance 10-100x for a state with 100 @PersistState properties.
     *
     * This is also @Synchronized to prevent a ConcurrentModificationException in kotlin-reflect: https://gist.github.com/gpeal/27a5747b3c351d4bd592a8d2d58f134a
     */
    @Synchronized
    fun warmReflectionCache(initialState: S) {
        initialState::class.primaryConstructor?.parameters?.forEach { it.annotations }
        initialState::class.declaredMemberProperties.asSequence()
            .filter { it.visibility == KVisibility.PUBLIC }
            .forEach { prop ->
                @Suppress("UNCHECKED_CAST")
                (prop as? KProperty1<S, Any?>)?.get(initialState)
            }
    }

    @CallSuper
    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
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
                    val changedProp = firstState::class.memberProperties.asSequence()
                        .map { it as KProperty1<S, *> }
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
                            "Impure reducer set on ${this@BaseMvRxViewModel::class.simpleName}! " +
                                "${changedProp.name} changed from ${changedProp.get(firstState)} " +
                                "to ${changedProp.get(secondState)}. " +
                                "Ensure that your state properties properly implement hashCode."
                        )
                    } else {
                        throw IllegalArgumentException(
                            "Impure reducer set on ${this@BaseMvRxViewModel::class.simpleName}! Differing states were provided by the same reducer." +
                                "Ensure that your state properties properly implement hashCode. First state: $firstState -> Second state: $secondState"
                        )
                    }
                }
                mutableStateChecker.onStateChanged(firstState)

                firstState
            }
        } else {
            stateStore.set(reducer)
        }
    }

    /**
     * Access the current ViewModel state. Takes a block of code that will be run after all current pending state
     * updates are processed. The `this` inside of the block is the state.
     */
    protected fun withState(block: (state: S) -> Unit) {
        stateStore.get(block)
    }

    /**
     * Validates a number of properties on the state class. This cannot be called from the main thread because it does
     * a fair amount of reflection.
     */
    private fun validateState(initialState: S) {
        if (state::class.visibility != KVisibility.PUBLIC) {
            throw IllegalStateException("Your state class ${state::class.qualifiedName} must be public.")
        }
        state::class.assertImmutability()
        val bundle = state.persistState(assertCollectionPersistability = true)
        bundle.restorePersistedState(initialState)
    }

    /**
     * Helper to map an Single to an Async property on the state object.
     */
    fun <T> Single<T>.execute(
        stateReducer: S.(Async<T>) -> S
    ) = toObservable().execute({ it }, null, stateReducer)

    /**
     * Helper to map an Single to an Async property on the state object.
     * @param mapper A map converting the observable type to the desired AsyncData type.
     * @param stateReducer A reducer that is applied to the current state and should return the
     *                     new state. Because the state is the receiver and it likely a data
     *                     class, an implementation may look like: `{ copy(response = it) }`.
     */
    fun <T, V> Single<T>.execute(
        mapper: (T) -> V,
        stateReducer: S.(Async<V>) -> S
    ) = toObservable().execute(mapper, null, stateReducer)

    /**
     * Helper to map an observable to an Async property on the state object.
     */
    fun <T> Observable<T>.execute(
        stateReducer: S.(Async<T>) -> S
    ) = execute({ it }, null, stateReducer)

    /**
     * Helper to map a Completable to an Async property on the state object.
     */
    fun Completable.execute(
        stateReducer: S.(Async<Unit>) -> S
    ) = toSingle { Unit }.execute(stateReducer)

    /**
     * Execute an observable and wrap its progression with AsyncData reduced to the global state.
     *
     * @param mapper A map converting the observable type to the desired AsyncData type.
     * @param successMetaData A map that provides metadata to set on the Success result.
     *                        It allows data about the original Observable to be kept and accessed later. For example,
     *                        your mapper could map a network request to just the data your UI needs, but your base layers could
     *                        keep metadata about the request, like timing, for logging.
     * @param stateReducer A reducer that is applied to the current state and should return the
     *                     new state. Because the state is the receiver and it likely a data
     *                     class, an implementation may look like: `{ copy(response = it) }`.
     *
     *  @see Success.metadata
     */
    fun <T, V> Observable<T>.execute(
        mapper: (T) -> V,
        successMetaData: ((T) -> Any)? = null,
        stateReducer: S.(Async<V>) -> S
    ): Disposable {
        setState { stateReducer(Loading()) }

        return map { value ->
            val success = Success(mapper(value))
            success.metadata = successMetaData?.invoke(value)
            success as Async<V>
        }
            .onErrorReturn {
                if (debugMode) Log.e(tag, "Observable encountered error", it)
                Fail(it)
            }
            .subscribe { asyncData -> setState { stateReducer(asyncData) } }
            .disposeOnClear()
    }

    /**
     * Output all state changes to logcat.
     */
    fun logStateChanges() {
        if (!debugMode) return
        subscribe { Log.d(tag, "New State: $it") }
    }

    /**
     * For ViewModels that want to subscribe to itself.
     */
    protected fun subscribe(subscriber: (S) -> Unit) =
        stateStore.observable.subscribeLifecycle(null, RedeliverOnStart, subscriber)

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun subscribe(owner: LifecycleOwner, deliveryMode: DeliveryMode = RedeliverOnStart, subscriber: (S) -> Unit) =
        stateStore.observable.subscribeLifecycle(owner, deliveryMode, subscriber)

    /**
     * Subscribe to state changes for only a single property.
     */
    protected fun <A> selectSubscribe(
        prop1: KProperty1<S, A>,
        subscriber: (A) -> Unit
    ) = selectSubscribeInternal(null, prop1, RedeliverOnStart, subscriber)

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun <A> selectSubscribe(
        owner: LifecycleOwner,
        prop1: KProperty1<S, A>,
        deliveryMode: DeliveryMode = RedeliverOnStart,
        subscriber: (A) -> Unit
    ) = selectSubscribeInternal(owner, prop1, deliveryMode, subscriber)

    private fun <A> selectSubscribeInternal(
        owner: LifecycleOwner?,
        prop1: KProperty1<S, A>,
        deliveryMode: DeliveryMode,
        subscriber: (A) -> Unit
    ) = stateStore.observable
        .map { MvRxTuple1(prop1.get(it)) }
        .distinctUntilChanged()
        .subscribeLifecycle(owner, deliveryMode.appendPropertiesToId(prop1)) { (a) -> subscriber(a) }

    /**
     * Subscribe to changes in an async property. There are optional parameters for onSuccess
     * and onFail which automatically unwrap the value or error.
     */
    protected fun <T> asyncSubscribe(
        asyncProp: KProperty1<S, Async<T>>,
        onFail: ((Throwable) -> Unit)? = null,
        onSuccess: ((T) -> Unit)? = null
    ) = asyncSubscribeInternal(null, asyncProp, RedeliverOnStart, onFail, onSuccess)

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun <T> asyncSubscribe(
        owner: LifecycleOwner,
        asyncProp: KProperty1<S, Async<T>>,
        deliveryMode: DeliveryMode = RedeliverOnStart,
        onFail: ((Throwable) -> Unit)? = null,
        onSuccess: ((T) -> Unit)? = null
    ) = asyncSubscribeInternal(owner, asyncProp, deliveryMode, onFail, onSuccess)

    private fun <T> asyncSubscribeInternal(
        owner: LifecycleOwner?,
        asyncProp: KProperty1<S, Async<T>>,
        deliveryMode: DeliveryMode,
        onFail: ((Throwable) -> Unit)? = null,
        onSuccess: ((T) -> Unit)? = null
    ) = selectSubscribeInternal(owner, asyncProp, deliveryMode.appendPropertiesToId(asyncProp)) { asyncValue ->
        if (onSuccess != null && asyncValue is Success) {
            onSuccess(asyncValue())
        } else if (onFail != null && asyncValue is Fail) {
            onFail(asyncValue.error)
        }
    }

    /**
     * Subscribe to state changes for two properties.
     */
    protected fun <A, B> selectSubscribe(
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        subscriber: (A, B) -> Unit
    ) = selectSubscribeInternal(null, prop1, prop2, RedeliverOnStart, subscriber)

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun <A, B> selectSubscribe(
        owner: LifecycleOwner,
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        deliveryMode: DeliveryMode = RedeliverOnStart,
        subscriber: (A, B) -> Unit
    ) = selectSubscribeInternal(owner, prop1, prop2, deliveryMode, subscriber)

    private fun <A, B> selectSubscribeInternal(
        owner: LifecycleOwner?,
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        deliveryMode: DeliveryMode,
        subscriber: (A, B) -> Unit
    ) = stateStore.observable
        .map { MvRxTuple2(prop1.get(it), prop2.get(it)) }
        .distinctUntilChanged()
        .subscribeLifecycle(owner, deliveryMode.appendPropertiesToId(prop1, prop2)) { (a, b) -> subscriber(a, b) }

    /**
     * Subscribe to state changes for three properties.
     */
    protected fun <A, B, C> selectSubscribe(
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        subscriber: (A, B, C) -> Unit
    ) = selectSubscribeInternal(null, prop1, prop2, prop3, RedeliverOnStart, subscriber)

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun <A, B, C> selectSubscribe(
        owner: LifecycleOwner,
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        deliveryMode: DeliveryMode = RedeliverOnStart,
        subscriber: (A, B, C) -> Unit
    ) = selectSubscribeInternal(owner, prop1, prop2, prop3, deliveryMode, subscriber)

    private fun <A, B, C> selectSubscribeInternal(
        owner: LifecycleOwner?,
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        deliveryMode: DeliveryMode,
        subscriber: (A, B, C) -> Unit
    ) = stateStore.observable
        .map { MvRxTuple3(prop1.get(it), prop2.get(it), prop3.get(it)) }
        .distinctUntilChanged()
        .subscribeLifecycle(owner, deliveryMode.appendPropertiesToId(prop1, prop2, prop3)) { (a, b, c) ->
            subscriber(
                a,
                b,
                c
            )
        }

    /**
     * Subscribe to state changes for four properties.
     */
    protected fun <A, B, C, D> selectSubscribe(
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        prop4: KProperty1<S, D>,
        subscriber: (A, B, C, D) -> Unit
    ) = selectSubscribeInternal(null, prop1, prop2, prop3, prop4, RedeliverOnStart, subscriber)

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun <A, B, C, D> selectSubscribe(
        owner: LifecycleOwner,
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        prop4: KProperty1<S, D>,
        deliveryMode: DeliveryMode = RedeliverOnStart,
        subscriber: (A, B, C, D) -> Unit
    ) = selectSubscribeInternal(owner, prop1, prop2, prop3, prop4, deliveryMode, subscriber)

    private fun <A, B, C, D> selectSubscribeInternal(
        owner: LifecycleOwner?,
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        prop4: KProperty1<S, D>,
        deliveryMode: DeliveryMode,
        subscriber: (A, B, C, D) -> Unit
    ) = stateStore.observable
        .map { MvRxTuple4(prop1.get(it), prop2.get(it), prop3.get(it), prop4.get(it)) }
        .distinctUntilChanged()
        .subscribeLifecycle(
            owner,
            deliveryMode.appendPropertiesToId(prop1, prop2, prop3, prop4)
        ) { (a, b, c, d) -> subscriber(a, b, c, d) }

    /**
     * Subscribe to state changes for five properties.
     */
    protected fun <A, B, C, D, E> selectSubscribe(
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        prop4: KProperty1<S, D>,
        prop5: KProperty1<S, E>,
        subscriber: (A, B, C, D, E) -> Unit
    ) = selectSubscribeInternal(null, prop1, prop2, prop3, prop4, prop5, RedeliverOnStart, subscriber)

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun <A, B, C, D, E> selectSubscribe(
        owner: LifecycleOwner,
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        prop4: KProperty1<S, D>,
        prop5: KProperty1<S, E>,
        deliveryMode: DeliveryMode = RedeliverOnStart,
        subscriber: (A, B, C, D, E) -> Unit
    ) = selectSubscribeInternal(owner, prop1, prop2, prop3, prop4, prop5, deliveryMode, subscriber)

    private fun <A, B, C, D, E> selectSubscribeInternal(
        owner: LifecycleOwner?,
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        prop4: KProperty1<S, D>,
        prop5: KProperty1<S, E>,
        deliveryMode: DeliveryMode,
        subscriber: (A, B, C, D, E) -> Unit
    ) = stateStore.observable
        .map { MvRxTuple5(prop1.get(it), prop2.get(it), prop3.get(it), prop4.get(it), prop5.get(it)) }
        .distinctUntilChanged()
        .subscribeLifecycle(
            owner,
            deliveryMode.appendPropertiesToId(prop1, prop2, prop3, prop4, prop5)
        ) { (a, b, c, d, e) -> subscriber(a, b, c, d, e) }

    /**
     * Subscribe to state changes for six properties.
     */
    protected fun <A, B, C, D, E, F> selectSubscribe(
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        prop4: KProperty1<S, D>,
        prop5: KProperty1<S, E>,
        prop6: KProperty1<S, F>,
        subscriber: (A, B, C, D, E, F) -> Unit
    ) = selectSubscribeInternal(null, prop1, prop2, prop3, prop4, prop5, prop6, RedeliverOnStart, subscriber)

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun <A, B, C, D, E, F> selectSubscribe(
        owner: LifecycleOwner,
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        prop4: KProperty1<S, D>,
        prop5: KProperty1<S, E>,
        prop6: KProperty1<S, F>,
        deliveryMode: DeliveryMode = RedeliverOnStart,
        subscriber: (A, B, C, D, E, F) -> Unit
    ) = selectSubscribeInternal(owner, prop1, prop2, prop3, prop4, prop5, prop6, deliveryMode, subscriber)

    private fun <A, B, C, D, E, F> selectSubscribeInternal(
        owner: LifecycleOwner?,
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        prop4: KProperty1<S, D>,
        prop5: KProperty1<S, E>,
        prop6: KProperty1<S, F>,
        deliveryMode: DeliveryMode,
        subscriber: (A, B, C, D, E, F) -> Unit
    ) = stateStore.observable
        .map { MvRxTuple6(prop1.get(it), prop2.get(it), prop3.get(it), prop4.get(it), prop5.get(it), prop6.get(it)) }
        .distinctUntilChanged()
        .subscribeLifecycle(
            owner,
            deliveryMode.appendPropertiesToId(prop1, prop2, prop3, prop4, prop5, prop6)
        ) { (a, b, c, d, e, f) -> subscriber(a, b, c, d, e, f) }

    /**
     * Subscribe to state changes for seven properties.
     */
    protected fun <A, B, C, D, E, F, G> selectSubscribe(
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        prop4: KProperty1<S, D>,
        prop5: KProperty1<S, E>,
        prop6: KProperty1<S, F>,
        prop7: KProperty1<S, G>,
        subscriber: (A, B, C, D, E, F, G) -> Unit
    ) = selectSubscribeInternal(null, prop1, prop2, prop3, prop4, prop5, prop6, prop7, RedeliverOnStart, subscriber)

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun <A, B, C, D, E, F, G> selectSubscribe(
        owner: LifecycleOwner,
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        prop4: KProperty1<S, D>,
        prop5: KProperty1<S, E>,
        prop6: KProperty1<S, F>,
        prop7: KProperty1<S, G>,
        deliveryMode: DeliveryMode = RedeliverOnStart,
        subscriber: (A, B, C, D, E, F, G) -> Unit
    ) = selectSubscribeInternal(owner, prop1, prop2, prop3, prop4, prop5, prop6, prop7, deliveryMode, subscriber)

    private fun <A, B, C, D, E, F, G> selectSubscribeInternal(
        owner: LifecycleOwner?,
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        prop4: KProperty1<S, D>,
        prop5: KProperty1<S, E>,
        prop6: KProperty1<S, F>,
        prop7: KProperty1<S, G>,
        deliveryMode: DeliveryMode,
        subscriber: (A, B, C, D, E, F, G) -> Unit
    ) = stateStore.observable
        .map { state ->
            MvRxTuple7(
                prop1.get(state),
                prop2.get(state),
                prop3.get(state),
                prop4.get(state),
                prop5.get(state),
                prop6.get(state),
                prop7.get(state)
            )
        }
        .distinctUntilChanged()
        .subscribeLifecycle(
            owner,
            deliveryMode.appendPropertiesToId(prop1, prop2, prop3, prop4, prop5, prop6, prop7)
        ) { (a, b, c, d, e, f, g) -> subscriber(a, b, c, d, e, f, g) }

    private fun <T : Any> Observable<T>.subscribeLifecycle(
        lifecycleOwner: LifecycleOwner? = null,
        deliveryMode: DeliveryMode,
        subscriber: (T) -> Unit
    ): Disposable {
        return observeOn(AndroidSchedulers.mainThread())
            .resolveSubscription(lifecycleOwner, deliveryMode, subscriber)
            .disposeOnClear()
    }

    private fun <T : Any> Observable<T>.resolveSubscription(
        lifecycleOwner: LifecycleOwner? = null,
        deliveryMode: DeliveryMode,
        subscriber: (T) -> Unit
    ): Disposable = if (lifecycleOwner == null || FORCE_DISABLE_LIFECYCLE_AWARE_OBSERVER) {
        this.subscribe(subscriber)
    } else {
        this.subscribeWith(
            MvRxLifecycleAwareObserver(
                lifecycleOwner,
                deliveryMode = deliveryMode,
                lastDeliveredValue = if (deliveryMode is UniqueOnly) {
                    if (activeSubscriptions.contains(deliveryMode.subscriptionId)) {
                        throw IllegalStateException(
                            "Subscribing with a duplicate subscription id: ${deliveryMode.subscriptionId}. " +
                                "If you have multiple uniqueOnly subscriptions in a MvRx view that listen to the same properties " +
                                "you must use a custom subscription id. If you are using a custom MvRxView, make sure you are using the proper" +
                                "lifecycle owner. See BaseMvRxFragment for an example."
                        )
                    }
                    activeSubscriptions.add(deliveryMode.subscriptionId)
                    lastDeliveredStates[deliveryMode.subscriptionId] as? T
                } else {
                    null
                },
                onNext = Consumer { value ->
                    if (deliveryMode is UniqueOnly) {
                        lastDeliveredStates[deliveryMode.subscriptionId] = value
                    }
                    subscriber(value)
                },
                onDispose = {
                    if (deliveryMode is UniqueOnly) {
                        activeSubscriptions.remove(deliveryMode.subscriptionId)
                    }
                }
            )
        )
    }

    protected fun Disposable.disposeOnClear(): Disposable {
        disposables.add(this)
        return this
    }

    override fun toString(): String = "${this::class.simpleName} $state"
}

/**
 * Defines what updates a subscription should receive.
 * See: [RedeliverOnStart], [UniqueOnly].
 */
sealed class DeliveryMode {

    internal fun appendPropertiesToId(vararg properties: KProperty1<*, *>): DeliveryMode {
        return when (this) {
            is RedeliverOnStart -> RedeliverOnStart
            is UniqueOnly -> UniqueOnly(subscriptionId + "_" + properties.joinToString(",") { it.name })
        }
    }
}

/**
 * The subscription will receive the most recent state update when transitioning from locked to unlocked states (stopped -> started),
 * even if the state has not changed while locked.
 *
 * Likewise, when a MvRxView resubscribes after a configuration change the most recent update will always be emitted.
 */
object RedeliverOnStart : DeliveryMode()

/**
 * The subscription will receive the most recent state update when transitioning from locked to unlocked states (stopped -> started),
 * only if the state has changed while locked.
 *
 * Likewise, when a MvRxView resubscribes after a configuration change the most recent update will only be emitted
 * if the state has changed while locked.
 *
 * @param subscriptionId A uniqueIdentifier for this subscription. It is an error for two unique only subscriptions to
 * have the same id.
 */
class UniqueOnly(val subscriptionId: String) : DeliveryMode()
