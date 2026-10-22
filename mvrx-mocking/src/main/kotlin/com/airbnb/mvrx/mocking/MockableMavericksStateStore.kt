package com.airbnb.mvrx.mocking

import com.airbnb.mvrx.CoroutinesStateStore
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.ScriptableMavericksStateStore
import com.airbnb.mvrx.ScriptableStateStore
import com.airbnb.mvrx.mocking.MockBehavior.StateStoreBehavior
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.isActive
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

interface MockableStateStore<S : Any> : ScriptableStateStore<S> {
    var mockBehavior: MockBehavior
}

/**
 * Allows switching between a mocked state store and a real state store behavior.
 * This is intended to enable testing.
 *
 * When [StateStoreBehavior.Scriptable] behavior is set, [next] can be called to force a state
 * synchronously. When in this mode, calls to [set] have no effect. In other modes,
 * it is an error to call [next].
 *
 * The mode can be dynamically changed to modify behavior at runtime.
 *
 * [set] and [next] are safe to call from several threads at once. In [StateStoreBehavior.Synchronous]
 * and [StateStoreBehavior.Scriptable] modes, each call holds a lock while it reads the current state
 * and updates the synchronous and scriptable stores (and queues an update on the coroutine store).
 * In [StateStoreBehavior.Normal] mode, [set] queues the reducer under the lock and applies it later
 * on the coroutine store's flush thread (or when draining before a mode switch or [next]), still
 * holding the lock while reading the current state and updating the stores so concurrent updates
 * cannot overwrite each other or leave the stores disagreeing.
 */
class MockableMavericksStateStore<S : MavericksState>(
    initialState: S,
    mockBehavior: MockBehavior,
    val coroutineScope: CoroutineScope,
    contextOverride: CoroutineContext = EmptyCoroutineContext
) : MockableStateStore<S> {
    private val scriptableStore = ScriptableMavericksStateStore(initialState)
    private val realStore = CoroutinesStateStore(initialState, coroutineScope, contextOverride)
    private val realImmediateStore = SynchronousMavericksStateStore(initialState)

    private val onStateSetListeners = CopyOnWriteArrayList<(previousState: S, newState: S) -> Unit>()
    private val pendingNormalReducers = ArrayDeque<S.() -> S>()

    /**
     * Guards [set] and [next]. The synchronous store takes its own lock inside this one, and never
     * the other way around, so the two cannot deadlock.
     */
    private val lock = Any()

    @Volatile
    override var mockBehavior: MockBehavior = mockBehavior
        set(value) {
            synchronized(lock) {
                field = value
                if (value.stateStoreBehavior != StateStoreBehavior.Normal) {
                    drainPendingNormalReducersLocked()
                }
            }
        }

    private val currentStore
        get() = when (mockBehavior.stateStoreBehavior) {
            StateStoreBehavior.Scriptable -> scriptableStore
            StateStoreBehavior.Normal -> realStore
            StateStoreBehavior.Synchronous -> realImmediateStore
        }

    override val state: S
        get() = realImmediateStore.state

    override fun next(state: S) {
        check(mockBehavior.stateStoreBehavior == StateStoreBehavior.Scriptable) {
            "Scriptable store is not enabled"
        }

        synchronized(lock) {
            drainPendingNormalReducersLocked()
            scriptableStore.next(state)
            // Update the real stores too, so if we switch to them they are already up to date.
            realImmediateStore.set { state }
        }
        syncRealStore()
    }

    override fun get(block: (S) -> Unit) {
        currentStore.get(block)
    }

    override fun set(stateReducer: S.() -> S) {
        val shouldSyncRealStore: Boolean
        synchronized(lock) {
            if (mockBehavior.stateStoreBehavior == StateStoreBehavior.Normal) {
                pendingNormalReducers.addLast(stateReducer)
                shouldSyncRealStore = true
            } else {
                drainPendingNormalReducersLocked()
                val previousState = state
                val newState = previousState.stateReducer()
                onStateSetListeners.forEach { it.invoke(previousState, newState) }

                // Setting state only takes effect is "scriptable" mode is not enabled.
                if (mockBehavior.stateStoreBehavior != StateStoreBehavior.Scriptable) {
                    // States are updated on all stores, so if we switch to any other in the future
                    // it will be correctly up to date.
                    realImmediateStore.set { newState }
                    scriptableStore.next(newState)
                    shouldSyncRealStore = true
                } else {
                    shouldSyncRealStore = false
                }
            }
        }
        if (shouldSyncRealStore) {
            syncRealStore()
        }
    }

    private fun syncRealStore() {
        realStore.set {
            synchronized(lock) {
                val reducer = pendingNormalReducers.removeFirstOrNull()
                if (reducer != null) {
                    applyNormalReducerLocked(reducer)
                }
                realImmediateStore.state
            }
        }
    }

    private fun drainPendingNormalReducersLocked() {
        while (pendingNormalReducers.isNotEmpty()) {
            applyNormalReducerLocked(pendingNormalReducers.removeFirst())
        }
    }

    private fun applyNormalReducerLocked(stateReducer: S.() -> S) {
        val previousState = realImmediateStore.state
        val newState = previousState.stateReducer()
        realImmediateStore.set { newState }
        scriptableStore.next(newState)
        onStateSetListeners.forEach { it.invoke(previousState, newState) }
    }

    /**
     * Add a listener that will be called each time a state is set on this state store via
     * the [set] function.
     *
     * In [StateStoreBehavior.Synchronous] and [StateStoreBehavior.Scriptable] modes, the callback
     * is invoked synchronously on the caller's thread before [set] returns. In
     * [StateStoreBehavior.Normal] mode, the reducer is queued and the callback is invoked later on
     * the store's flush thread when the reducer is applied (or on the thread that switches modes or
     * calls [next], if pending reducers are drained first).
     */
    fun addOnStateSetListener(callback: (previousState: S, newState: S) -> Unit) {
        onStateSetListeners.add(callback)
    }

    fun removeOnStateSetListener(callback: (previousState: S, newState: S) -> Unit) {
        onStateSetListeners.remove(callback)
    }

    fun addOnCancelListener(callback: (MockableMavericksStateStore<*>) -> Unit) {
        if (coroutineScope.isActive) {
            coroutineScope.coroutineContext[Job]!!.invokeOnCompletion {
                callback(this)
            }
        } else {
            callback(this)
        }
    }

    override val flow: Flow<S>
        // TODO: If flow is provided and then state store switches, implementation of provided slow won't retroactively update
        get() = currentStore.flow
}
