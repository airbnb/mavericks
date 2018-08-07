package com.airbnb.mvrx

import android.annotation.SuppressLint
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.ViewModel
import android.os.Looper
import android.support.annotation.CallSuper
import android.util.Log
import com.airbnb.android.lib.mvrx.base.assertImmutability
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KVisibility

abstract class BaseMvRxViewModel<S : MvRxState> : ViewModel() {
    private val tag by lazy { javaClass.simpleName }
    private val disposables = CompositeDisposable()
    private val backgroundScheduler = Schedulers.single()

    /**
     * This has to be lazy so that initialState can be initialized in the child.
     */
    private val stateStore: MvRxStateStore<S> by lazy { MvRxStateStore(initialState) }

    /**
     * Enable debug features which check for certain properties like idempotent reducers and immutable state.
     */
    protected abstract val debugMode: Boolean

    internal val state: S by object : ReadOnlyProperty<BaseMvRxViewModel<S>, S> {
        private var hasValidatedState = false

        override fun getValue(thisRef: BaseMvRxViewModel<S>, property: KProperty<*>) = thisRef.stateStore.state.apply {
            if (debugMode && !hasValidatedState) {
                Observable.fromCallable { validateState() }
                    .subscribeOn(Schedulers.computation())
                    .subscribe()
                hasValidatedState = true
            }
        }
    }

    /**
     * Override this to provide the initial state.
     */
    protected abstract val initialState: S

    @CallSuper override fun onCleared() {
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
     *    mutable variables or properties from outside the lambda or else it may crash. Again, in this case,
     */
    protected fun setState(reducer: S.() -> S) {
        if (debugMode) {
            val state = state
            val firstState = state.reducer()
            val secondState = state.reducer()
            if (firstState != secondState) throw IllegalArgumentException("Your reducer must be pure!")
        }
        stateStore.set(reducer)
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
    @SuppressLint("VisibleForTests")
    fun validateState() {
        if (Looper.myLooper() == Looper.getMainLooper()) throw IllegalStateException("validateState should not be called from the main thread.")
        if (state::class.visibility != KVisibility.PUBLIC) throw IllegalStateException("Your state class must be public.")
        state::class.assertImmutability()
        val bundle = state.persistState(assertCollectionPersistability = true)
        bundle.restorePersistedState(initialState)
    }

    /**
     * Helper to map an Single to an Async property on the state object.
     */
    fun <T> Single<T>.execute(
        stateReducer: S.(Async<T>) -> S
    ) = toObservable().execute({ it }, stateReducer)

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
    ) = toObservable().execute(mapper, stateReducer)

    /**
     * Helper to map an observable to an Async property on the state object.
     */
    fun <T> Observable<T>.execute(
        stateReducer: S.(Async<T>) -> S
    ) = execute({ it }, stateReducer)

    /**
     * Execute an observable and wrap its progression with AsyncData reduced to the global state.
     *
     * @param mapper A map converting the observable type to the desired AsyncData type.
     * @param stateReducer A reducer that is applied to the current state and should return the
     *                     new state. Because the state is the receiver and it likely a data
     *                     class, an implementation may look like: `{ copy(response = it) }`.
     */
    fun <T, V> Observable<T>.execute(
        mapper: (T) -> V,
        stateReducer: S.(Async<V>) -> S
    ): Disposable {
        // This will ensure that Loading is dispatched immediately rather than being posted to `backgroundScheduler` before emitting Loading.
        setState { stateReducer(Loading()) }

        return observeOn(backgroundScheduler)
            .subscribeOn(backgroundScheduler)
            .map(mapper)
            .map { Success(it) as Async<V> }
            .onErrorReturn { Fail(it) }
            .subscribe { asyncData -> setState { stateReducer(asyncData) } }
            .disposeOnClear()
    }

    /**
     * Output all state changes to logcat.
     */
    fun logStateChanges() {
        if (!debugMode) return
        stateStore
            .subscribe { Log.d(tag, "New State: $it") }
            .disposeOnClear()
    }

    /**
     * Subscribe to state updates.
     *
     * This is only open so it can be mocked for testing. Do not extend it.
     *
     * @param owner The LifecycleOwner such as a Fragment or Activity that wants to subscribe to
     *                     state updates.
     * @param shouldUpdate A lambda that takes the previous and new state and retuns whether the
     *                     subscriber should be notified.
     * @param subscriber A lambda that will get called every time the state changes.
     */
    fun subscribe(
        owner: LifecycleOwner,
        shouldUpdate: ((S, S) -> Boolean)? = null,
        observerScheduler: Scheduler = AndroidSchedulers.mainThread(),
        subscriber: (S) -> Unit
    ) {
        stateStore
            .subscribe(owner, observerScheduler, shouldUpdate, subscriber)
            .disposeOnClear()
    }

    /**
     * For ViewModels that want to subscribe to themself.
     */
    protected fun subscribe(
        shouldUpdate: (((S, S) -> Boolean))? = null,
        observerScheduler: Scheduler = AndroidSchedulers.mainThread(),
        subscriber: (S) -> Unit
    ) {
        stateStore
            .subscribe(null, observerScheduler, shouldUpdate, subscriber)
            .disposeOnClear()
    }

    /**
     * Subscribe to state updates. Includes the previous state.
     *
     * @see subscribe
     */
    fun subscribeWithHistory(
        owner: LifecycleOwner? = null,
        shouldUpdate: ((S, S) -> Boolean)? = null,
        observerScheduler: Scheduler = AndroidSchedulers.mainThread(),
        subscriber: (S, S) -> Unit
    ) = stateStore
        .subscribeWithHistory(
            owner,
            observerScheduler = observerScheduler,
            shouldUpdate = shouldUpdate,
            subscriber = subscriber
        )
        .disposeOnClear()

    protected fun Disposable.disposeOnClear(): Disposable {
        disposables.add(this)
        return this
    }

    override fun toString(): String = "${this::class.simpleName} $state"
}