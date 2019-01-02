package com.airbnb.mvrx

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.ViewModel
import android.support.annotation.CallSuper
import android.support.annotation.RestrictTo
import android.util.Log
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * To use MvRx, create your own base MvRxViewModel that extends this one and sets debugMode.
 *
 * All subsequent ViewModels in your app should use that one.
 */
abstract class BaseMvRxViewModel<S : MvRxState>(
    initialState: S,
    debugMode: Boolean = false,
    private val stateStore: MvRxStateStore<S> = RealMvRxStateStore(initialState)
) : ViewModel() {
    private val debugMode = if (MvRxTestOverrides.FORCE_DEBUG == null) debugMode else MvRxTestOverrides.FORCE_DEBUG

    private val tag by lazy { javaClass.simpleName }
    private val disposables = CompositeDisposable()
    private lateinit var mutableStateChecker: MutableStateChecker<S>

    init {
        // Kotlin reflection has a large overhead the first time you run it
        // but then is pretty fast on subsequent times. Running these methods now will
        // initialize kotlin reflect and warm the cache so that when persistState() gets
        // called synchronously in onSaveInstanceState() on the main thread, it will be
        // much faster.
        // This improved performance 10-100x for a state with 100 @PersistStae properties.
        Completable.fromCallable {
            initialState::class.primaryConstructor?.parameters?.forEach { it.annotations }
            initialState::class.declaredMemberProperties.forEach {
                @Suppress("UNCHECKED_CAST")
                (it as? KProperty1<S, Any?>)?.get(initialState)
            }
        }.subscribeOn(Schedulers.computation()).subscribe()

        if (this.debugMode) {
            mutableStateChecker = MutableStateChecker(initialState)

            Completable.fromCallable { validateState(initialState) }
                .subscribeOn(Schedulers.computation()).subscribe()
        }
    }

    internal val state: S
        get() = stateStore.state

    /**
     * Override this to provide the initial state.
     */
    @CallSuper
    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }

    /**
     * Call this to mutate the current state.
     * A few important notes about the state reducer.
     * 1) It will not be called synchronously or on the same thread. This is for performance and accuracy reasons.
     * 2) Similar to the execute lambda above, the current state is the state receiver  so the `count` in `count + 1` is actually the count
     *    property of the state at the time that the lambda is called
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
                    val changedProp = firstState::class.memberProperties
                            .map { it as KProperty1<S, *> }
                            .first { it.get(firstState) != it.get(secondState) }
                    throw IllegalArgumentException("Your reducer must be pure! ${changedProp.name} changed from " +
                            "${changedProp.get(firstState)} to ${changedProp.get(secondState)}. " +
                            "Ensure that your state properties properly implement hashCode.")
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

        return map {
            val success = Success(mapper(it))
            success.metadata = successMetaData?.invoke(it)
            success as Async<V>
        }
            .onErrorReturn { Fail(it) }
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
        stateStore.observable.subscribeLifecycle(null, false, subscriber)

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun subscribe(owner: LifecycleOwner, uniqueOnly: Boolean = false, subscriber: (S) -> Unit) =
        stateStore.observable.subscribeLifecycle(owner, uniqueOnly, subscriber)

    /**
     * Subscribe to state changes for only a single property.
     */
    protected fun <A> selectSubscribe(
        prop1: KProperty1<S, A>,
        subscriber: (A) -> Unit
    ) = selectSubscribeInternal(null, prop1, false, subscriber)

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun <A> selectSubscribe(
        owner: LifecycleOwner,
        prop1: KProperty1<S, A>,
        uniqueOnly: Boolean = false,
        subscriber: (A) -> Unit
    ) = selectSubscribeInternal(owner, prop1, uniqueOnly, subscriber)

    private fun <A> selectSubscribeInternal(
        owner: LifecycleOwner?,
        prop1: KProperty1<S, A>,
        uniqueOnly: Boolean = false,
        subscriber: (A) -> Unit
    ) = stateStore.observable
        .map { MvRxTuple1(prop1.get(it)) }
        .distinctUntilChanged()
        .subscribeLifecycle(owner, uniqueOnly) { (a) -> subscriber(a) }

    /**
     * Subscribe to changes in an async property. There are optional parameters for onSuccess
     * and onFail which automatically unwrap the value or error.
     */
    protected fun <T> asyncSubscribe(
        asyncProp: KProperty1<S, Async<T>>,
        onFail: ((Throwable) -> Unit)? = null,
        onSuccess: ((T) -> Unit)? = null
    ) = asyncSubscribeInternal(null, asyncProp, false, onFail, onSuccess)

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun <T> asyncSubscribe(
        owner: LifecycleOwner,
        asyncProp: KProperty1<S, Async<T>>,
        uniqueOnly: Boolean = false,
        onFail: ((Throwable) -> Unit)? = null,
        onSuccess: ((T) -> Unit)? = null
    ) = asyncSubscribeInternal(owner, asyncProp, uniqueOnly, onFail, onSuccess)

    private fun <T> asyncSubscribeInternal(
        owner: LifecycleOwner?,
        asyncProp: KProperty1<S, Async<T>>,
        uniqueOnly: Boolean = false,
        onFail: ((Throwable) -> Unit)? = null,
        onSuccess: ((T) -> Unit)? = null
    ) = selectSubscribeInternal(owner, asyncProp, uniqueOnly) {
        if (onSuccess != null && it is Success) {
            onSuccess(it())
        } else if (onFail != null && it is Fail) {
            onFail(it.error)
        }
    }

    /**
     * Subscribe to state changes for two properties.
     */
    protected fun <A, B> selectSubscribe(
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        subscriber: (A, B) -> Unit
    ) = selectSubscribeInternal(null, prop1, prop2, false, subscriber)

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun <A, B> selectSubscribe(
        owner: LifecycleOwner,
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        uniqueOnly: Boolean = false,
        subscriber: (A, B) -> Unit
    ) = selectSubscribeInternal(owner, prop1, prop2, uniqueOnly, subscriber)

    private fun <A, B> selectSubscribeInternal(
        owner: LifecycleOwner?,
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        uniqueOnly: Boolean = false,
        subscriber: (A, B) -> Unit
    ) = stateStore.observable
        .map { MvRxTuple2(prop1.get(it), prop2.get(it)) }
        .distinctUntilChanged()
        .subscribeLifecycle(owner, uniqueOnly) { (a, b) -> subscriber(a, b) }

    /**
     * Subscribe to state changes for three properties.
     */
    protected fun <A, B, C> selectSubscribe(
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        subscriber: (A, B, C) -> Unit
    ) = selectSubscribeInternal(null, prop1, prop2, prop3, false, subscriber)

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun <A, B, C> selectSubscribe(
        owner: LifecycleOwner,
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        uniqueOnly: Boolean = false,
        subscriber: (A, B, C) -> Unit
    ) = selectSubscribeInternal(owner, prop1, prop2, prop3, uniqueOnly, subscriber)

    private fun <A, B, C> selectSubscribeInternal(
        owner: LifecycleOwner?,
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        uniqueOnly: Boolean = false,
        subscriber: (A, B, C) -> Unit
    ) = stateStore.observable
        .map { MvRxTuple3(prop1.get(it), prop2.get(it), prop3.get(it)) }
        .distinctUntilChanged()
        .subscribeLifecycle(owner, uniqueOnly) { (a, b, c) -> subscriber(a, b, c) }

    /**
     * Subscribe to state changes for four properties.
     */
    protected fun <A, B, C, D> selectSubscribe(
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        prop4: KProperty1<S, D>,
        subscriber: (A, B, C, D) -> Unit
    ) = selectSubscribeInternal(null, prop1, prop2, prop3, prop4, false, subscriber)

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun <A, B, C, D> selectSubscribe(
        owner: LifecycleOwner,
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        prop4: KProperty1<S, D>,
        uniqueOnly: Boolean = false,
        subscriber: (A, B, C, D) -> Unit
    ) = selectSubscribeInternal(owner, prop1, prop2, prop3, prop4, uniqueOnly, subscriber)

    private fun <A, B, C, D> selectSubscribeInternal(
        owner: LifecycleOwner?,
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        prop4: KProperty1<S, D>,
        uniqueOnly: Boolean,
        subscriber: (A, B, C, D) -> Unit
    ) = stateStore.observable
        .map { MvRxTuple4(prop1.get(it), prop2.get(it), prop3.get(it), prop4.get(it)) }
        .distinctUntilChanged()
        .subscribeLifecycle(owner, uniqueOnly) { (a, b, c, d) -> subscriber(a, b, c, d) }

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
    ) = selectSubscribeInternal(null, prop1, prop2, prop3, prop4, prop5, false, subscriber)

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun <A, B, C, D, E> selectSubscribe(
            owner: LifecycleOwner,
            prop1: KProperty1<S, A>,
            prop2: KProperty1<S, B>,
            prop3: KProperty1<S, C>,
            prop4: KProperty1<S, D>,
            prop5: KProperty1<S, E>,
            uniqueOnly: Boolean = false,
            subscriber: (A, B, C, D, E) -> Unit
    ) = selectSubscribeInternal(owner, prop1, prop2, prop3, prop4, prop5, uniqueOnly, subscriber)

    private fun <A, B, C, D, E> selectSubscribeInternal(
            owner: LifecycleOwner?,
            prop1: KProperty1<S, A>,
            prop2: KProperty1<S, B>,
            prop3: KProperty1<S, C>,
            prop4: KProperty1<S, D>,
            prop5: KProperty1<S, E>,
            uniqueOnly: Boolean,
            subscriber: (A, B, C, D, E) -> Unit
    ) = stateStore.observable
            .map { MvRxTuple5(prop1.get(it), prop2.get(it), prop3.get(it), prop4.get(it), prop5.get(it)) }
            .distinctUntilChanged()
            .subscribeLifecycle(owner, uniqueOnly) { (a, b, c, d, e) -> subscriber(a, b, c, d, e) }

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
    ) = selectSubscribeInternal(null, prop1, prop2, prop3, prop4, prop5, prop6, false, subscriber)

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun <A, B, C, D, E, F> selectSubscribe(
            owner: LifecycleOwner,
            prop1: KProperty1<S, A>,
            prop2: KProperty1<S, B>,
            prop3: KProperty1<S, C>,
            prop4: KProperty1<S, D>,
            prop5: KProperty1<S, E>,
            prop6: KProperty1<S, F>,
            uniqueOnly: Boolean = false,
            subscriber: (A, B, C, D, E, F) -> Unit
    ) = selectSubscribeInternal(owner, prop1, prop2, prop3, prop4, prop5, prop6, uniqueOnly, subscriber)

    private fun <A, B, C, D, E, F> selectSubscribeInternal(
            owner: LifecycleOwner?,
            prop1: KProperty1<S, A>,
            prop2: KProperty1<S, B>,
            prop3: KProperty1<S, C>,
            prop4: KProperty1<S, D>,
            prop5: KProperty1<S, E>,
            prop6: KProperty1<S, F>,
            uniqueOnly: Boolean,
            subscriber: (A, B, C, D, E, F) -> Unit
    ) = stateStore.observable
            .map { MvRxTuple6(prop1.get(it), prop2.get(it), prop3.get(it), prop4.get(it), prop5.get(it), prop6.get(it)) }
            .distinctUntilChanged()
            .subscribeLifecycle(owner, uniqueOnly) { (a, b, c, d, e, f) -> subscriber(a, b, c, d, e, f) }

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
    ) = selectSubscribeInternal(null, prop1, prop2, prop3, prop4, prop5, prop6, prop7, false, subscriber)

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
            uniqueOnly: Boolean = false,
            subscriber: (A, B, C, D, E, F, G) -> Unit
    ) = selectSubscribeInternal(owner, prop1, prop2, prop3, prop4, prop5, prop6, prop7, uniqueOnly, subscriber)

    private fun <A, B, C, D, E, F, G> selectSubscribeInternal(
            owner: LifecycleOwner?,
            prop1: KProperty1<S, A>,
            prop2: KProperty1<S, B>,
            prop3: KProperty1<S, C>,
            prop4: KProperty1<S, D>,
            prop5: KProperty1<S, E>,
            prop6: KProperty1<S, F>,
            prop7: KProperty1<S, G>,
            uniqueOnly: Boolean,
            subscriber: (A, B, C, D, E, F, G) -> Unit
    ) = stateStore.observable
            .map { MvRxTuple7(prop1.get(it), prop2.get(it), prop3.get(it), prop4.get(it), prop5.get(it), prop6.get(it), prop7.get(it)) }
            .distinctUntilChanged()
            .subscribeLifecycle(owner, uniqueOnly) { (a, b, c, d, e, f, g) -> subscriber(a, b, c, d, e, f, g) }

    private fun <T> Observable<T>.subscribeLifecycle(
        lifecycleOwner: LifecycleOwner? = null,
        uniqueOnly: Boolean,
        subscriber: (T) -> Unit
    ): Disposable {
        if (lifecycleOwner == null) {
            return observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber)
                .disposeOnClear()
        }

        val lifecycleAwareObserver = MvRxLifecycleAwareObserver(
            lifecycleOwner,
            alwaysDeliverLastValueWhenUnlocked = !uniqueOnly,
            onNext = Consumer<T> { subscriber(it) }
        )
        return observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(lifecycleAwareObserver)
            .disposeOnClear()
    }

    protected fun Disposable.disposeOnClear(): Disposable {
        disposables.add(this)
        return this
    }

    override fun toString(): String = "${this::class.simpleName} $state"
}
