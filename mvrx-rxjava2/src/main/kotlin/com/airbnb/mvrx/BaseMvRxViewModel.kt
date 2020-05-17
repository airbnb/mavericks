package com.airbnb.mvrx

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModel
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KProperty1

/**
 * To use MvRx, create your own base MvRxViewModel that extends this one and sets debugMode.
 *
 * All subsequent ViewModels in your app should use that one.
 */
abstract class BaseMvRxViewModel<S : MvRxState>(
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
) : BaseMavericksViewModel<S>(initialState, debugMode, stateStoreOverride, contextOverride) {
    private val tag by lazy { javaClass.simpleName }
    private val disposables = CompositeDisposable()
    private val lastDeliveredStates = ConcurrentHashMap<String, Any>()
    private val activeSubscriptions = Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())

    /**
     * Define a [LifecycleOwner] to control subscriptions between [BaseMvRxViewModel]s. This only
     * provides two states, [Lifecycle.State.RESUMED] and [Lifecycle.State.DESTROYED] as it follows
     * the [ViewModel] object lifecycle. That is, when instantiated the lifecycle will be
     * [Lifecycle.State.RESUMED] and when [ViewModel.onCleared] is called the lifecycle will be
     * [Lifecycle.State.DESTROYED].
     *
     * This is not publicly accessible as it should only be used to control subscriptions
     * between two view models.
     */
    private val lifecycleOwner: LifecycleOwner = LifecycleOwner { lifecycleRegistry }
    private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(lifecycleOwner).apply { currentState = Lifecycle.State.RESUMED }

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }

    /**
     * Helper to map a [Single] to an [Async] property on the state object.
     */
    fun <T> Single<T>.execute(
        stateReducer: S.(Async<T>) -> S
    ) = toObservable().execute({ it }, null, stateReducer)

    /**
     * Helper to map a [Single] to an [Async] property on the state object.
     * @param mapper A map converting the Single type to the desired Async type.
     * @param stateReducer A reducer that is applied to the current state and should return the
     *                     new state. Because the state is the receiver and it likely a data
     *                     class, an implementation may look like: `{ copy(response = it) }`.
     */
    fun <T, V> Single<T>.execute(
        mapper: (T) -> V,
        stateReducer: S.(Async<V>) -> S
    ) = toObservable().execute(mapper, null, stateReducer)

    /**
     * Helper to map an [Observable] to an [Async] property on the state object.
     */
    fun <T> Observable<T>.execute(
        stateReducer: S.(Async<T>) -> S
    ) = execute({ it }, null, stateReducer)

    /**
     * Helper to map a [Completable] to an [Async] property on the state object.
     */
    fun Completable.execute(
        stateReducer: S.(Async<Unit>) -> S
    ) = toSingle { Unit }.execute(stateReducer)

    /**
     * Execute an [Observable] and wrap its progression with [Async] property reduced to the global state.
     *
     * @param mapper A map converting the Observable type to the desired Async type.
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
        // Intentionally didn't use RxJava's startWith operator. When withState is called right after execute then the loading reducer won't be enqueued yet if startWith is used.
        setState { stateReducer(Loading()) }

        return map<Async<V>> { value ->
            val success = Success(mapper(value))
            success.metadata = successMetaData?.invoke(value)
            success
        }
            .onErrorReturn { e ->
                Fail(e)
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
    protected fun subscribe(subscriber: (S) -> Unit): Disposable = onEachInternal(null, action = { subscriber(it) }).toDisposable()

    /**
     * For ViewModels that want to subscribe to another ViewModel.
     */
    protected fun <S : MvRxState> subscribe(
        viewModel: BaseMvRxViewModel<S>,
        subscriber: (S) -> Unit
    ) {
        assertSubscribeToDifferentViewModel(viewModel)
        viewModel.onEachInternal(viewModel.lifecycleOwner, action = { subscriber(it) }).toDisposable()
    }

    /**
     * Subscribe to state changes for only a single property.
     */
    protected fun <A> selectSubscribe(
        prop1: KProperty1<S, A>,
        subscriber: (A) -> Unit
    ) = onEach1Internal(null, prop1, action = { subscriber(it) }).toDisposable()

    /**
     * Subscribe to state changes for only a single property in a different ViewModel.
     */
    protected fun <A, S : MvRxState> selectSubscribe(
        viewModel: BaseMvRxViewModel<S>,
        prop1: KProperty1<S, A>,
        subscriber: (A) -> Unit
    ) {
        assertSubscribeToDifferentViewModel(viewModel)
        viewModel.onEach1Internal(lifecycleOwner, prop1, action = { subscriber(it) }).toDisposable()
    }

    private fun <A> selectSubscribeInternal(
        owner: LifecycleOwner?,
        prop1: KProperty1<S, A>,
        deliveryMode: DeliveryMode,
        subscriber: (A) -> Unit
    ) = onEach1Internal(owner, prop1, deliveryMode, { subscriber(it) }).toDisposable()

    /**
     * Subscribe to changes in an async property. There are optional parameters for onSuccess
     * and onFail which automatically unwrap the value or error.
     */
    protected fun <T> asyncSubscribe(
        asyncProp: KProperty1<S, Async<T>>,
        onFail: ((Throwable) -> Unit)? = null,
        onSuccess: ((T) -> Unit)? = null
    ) = onAsyncInternal(null, asyncProp, onFail = { onFail?.invoke(it) }, onSuccess = { onSuccess?.invoke(it) }).toDisposable()

    /**
     * Subscribe to changes in an async property in a different ViewModel. There are optional parameters
     * for onSuccess and onFail which automatically unwrap the value or error.
     */
    protected fun <T, S : MvRxState> asyncSubscribe(
        viewModel: BaseMvRxViewModel<S>,
        asyncProp: KProperty1<S, Async<T>>,
        onFail: ((Throwable) -> Unit)? = null,
        onSuccess: ((T) -> Unit)? = null
    ) {
        assertSubscribeToDifferentViewModel(viewModel)
        viewModel.onAsyncInternal(lifecycleOwner, asyncProp, onFail = { onFail?.invoke(it) }, onSuccess = { onSuccess?.invoke(it) })
            .toDisposable()
    }

    /**
     * Subscribe to state changes for two properties.
     */
    protected fun <A, B> selectSubscribe(
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        subscriber: (A, B) -> Unit
    ) = onEach2Internal(null, prop1, prop2, action = { a, b -> subscriber(a, b) }).toDisposable()

    /**
     * Subscribe to state changes for two properties in a different ViewModel.
     */
    protected fun <A, B, S : MvRxState> selectSubscribe(
        viewModel: BaseMvRxViewModel<S>,
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        subscriber: (A, B) -> Unit
    ) {
        assertSubscribeToDifferentViewModel(viewModel)
        viewModel.onEach2Internal(lifecycleOwner, prop1, prop2, action = { a, b -> subscriber(a, b) }).toDisposable()
    }

    /**
     * Subscribe to state changes for three properties.
     */
    protected fun <A, B, C> selectSubscribe(
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        subscriber: (A, B, C) -> Unit
    ) = onEach3Internal(null, prop1, prop2, prop3, action = { a, b, c -> subscriber(a, b, c) }).toDisposable()

    /**
     * Subscribe to state changes for three properties in a different ViewModel.
     */
    protected fun <A, B, C, S : MvRxState> selectSubscribe(
        viewModel: BaseMvRxViewModel<S>,
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        subscriber: (A, B, C) -> Unit
    ) {
        assertSubscribeToDifferentViewModel(viewModel)
        viewModel.onEach3Internal(lifecycleOwner, prop1, prop2, prop3, action = { a, b, c -> subscriber(a, b, c) }).toDisposable()
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
    ) = onEach4Internal(null, prop1, prop2, prop3, prop4, RedeliverOnStart, { a, b, c, d -> subscriber(a, b, c, d) }).toDisposable()

    /**
     * Subscribe to state changes for four properties in a different ViewModel.
     */
    protected fun <A, B, C, D, S : MvRxState> selectSubscribe(
        viewModel: BaseMvRxViewModel<S>,
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        prop4: KProperty1<S, D>,
        subscriber: (A, B, C, D) -> Unit
    ) {
        assertSubscribeToDifferentViewModel(viewModel)
        viewModel.onEach4Internal(lifecycleOwner, prop1, prop2, prop3, prop4, RedeliverOnStart, { a, b, c, d -> subscriber(a, b, c, d) })
            .toDisposable()
    }

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
    ) = onEach5Internal(null, prop1, prop2, prop3, prop4, prop5, RedeliverOnStart, { a, b, c, d, e ->
        subscriber(a, b, c, d, e)
    }).toDisposable()

    /**
     * Subscribe to state changes for five properties in a different ViewModel.
     */
    protected fun <A, B, C, D, E, S : MvRxState> selectSubscribe(
        viewModel: BaseMvRxViewModel<S>,
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        prop4: KProperty1<S, D>,
        prop5: KProperty1<S, E>,
        subscriber: (A, B, C, D, E) -> Unit
    ) {
        assertSubscribeToDifferentViewModel(viewModel)
        viewModel.onEach5Internal(lifecycleOwner, prop1, prop2, prop3, prop4, prop5, RedeliverOnStart, { a, b, c, d, e -> subscriber(a, b, c, d, e) })
            .toDisposable()
    }

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
    ) = onEach6Internal(null, prop1, prop2, prop3, prop4, prop5, prop6, RedeliverOnStart, { a, b, c, d, e, f ->
        subscriber(a, b, c, d, e, f)
    }).toDisposable()

    /**
     * Subscribe to state changes for six properties in a different ViewModel.
     */
    protected fun <A, B, C, D, E, F, S : MvRxState> selectSubscribe(
        viewModel: BaseMvRxViewModel<S>,
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        prop4: KProperty1<S, D>,
        prop5: KProperty1<S, E>,
        prop6: KProperty1<S, F>,
        subscriber: (A, B, C, D, E, F) -> Unit
    ) {
        assertSubscribeToDifferentViewModel(viewModel)
        viewModel.onEach6Internal(lifecycleOwner, prop1, prop2, prop3, prop4, prop5, prop6, RedeliverOnStart, { a, b, c, d, e, f ->
            subscriber(a, b, c, d, e, f)
        }).toDisposable()
    }

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
    ) = onEach7Internal(null, prop1, prop2, prop3, prop4, prop5, prop6, prop7, RedeliverOnStart, { a, b, c, d, e, f, g ->
        subscriber(a, b, c, d, e, f, g)
    }).toDisposable()

    /**
     * Subscribe to state changes for seven properties in a different ViewModel.
     */
    protected fun <A, B, C, D, E, F, G, S : MvRxState> selectSubscribe(
        viewModel: BaseMvRxViewModel<S>,
        prop1: KProperty1<S, A>,
        prop2: KProperty1<S, B>,
        prop3: KProperty1<S, C>,
        prop4: KProperty1<S, D>,
        prop5: KProperty1<S, E>,
        prop6: KProperty1<S, F>,
        prop7: KProperty1<S, G>,
        subscriber: (A, B, C, D, E, F, G) -> Unit
    ) {
        assertSubscribeToDifferentViewModel(viewModel)
        viewModel.onEach7Internal(lifecycleOwner, prop1, prop2, prop3, prop4, prop5, prop6, prop7, RedeliverOnStart, { a, b, c, d, e, f, g ->
            subscriber(a, b, c, d, e, f, g)
        }).toDisposable()
    }

    protected fun Disposable.disposeOnClear(): Disposable {
        disposables.add(this)
        return this
    }

    private fun Job.toDisposable() = Disposables.fromAction {
        cancel()
    }

    private fun <S : MvRxState> assertSubscribeToDifferentViewModel(viewModel: BaseMvRxViewModel<S>) {
        require(this != viewModel) {
            "This method is for subscribing to other view models. Please pass a different instance as the argument."
        }
    }

    private operator fun CoroutineContext.plus(other: CoroutineContext?) = if (other == null) this else this + other
}
