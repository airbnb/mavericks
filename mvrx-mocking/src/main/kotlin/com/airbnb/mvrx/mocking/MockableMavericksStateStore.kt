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
 * [set] and [next] are safe to call from several threads at once. Each call holds a lock while it
 * reads the current state and updates every underlying store, so concurrent updates cannot
 * overwrite each other or leave the stores disagreeing.
 */
class MockableMavericksStateStore<S : MavericksState>(
    initialState: S,
    override var mockBehavior: MockBehavior,
    val coroutineScope: CoroutineScope,
    contextOverride: CoroutineContext = EmptyCoroutineContext
) : MockableStateStore<S> {
    private val scriptableStore = ScriptableMavericksStateStore(initialState)
    private val realStore = CoroutinesStateStore(initialState, coroutineScope, contextOverride)
    private val realImmediateStore = SynchronousMavericksStateStore(initialState)

    private val onStateSetListeners = mutableListOf<(previousState: S, newState: S) -> Unit>()

    /**
     * Guards [set] and [next]. The synchronous store takes its own lock inside this one, and never
     * the other way around, so the two cannot deadlock.
     */
    private val lock = Any()

    private val currentStore
        get() = when (mockBehavior.stateStoreBehavior) {
            StateStoreBehavior.Scriptable -> scriptableStore
            StateStoreBehavior.Normal -> realStore
            StateStoreBehavior.Synchronous -> realImmediateStore
        }

    override val state: S
        get() = currentStore.state

    override fun next(state: S) {
        check(mockBehavior.stateStoreBehavior == StateStoreBehavior.Scriptable) {
            "Scriptable store is not enabled"
        }

        synchronized(lock) {
            scriptableStore.next(state)
            // Update the real stores too, so if we switch to them they are already up to date.
            realStore.set { state }
            realImmediateStore.set { state }
        }
    }

    override fun get(block: (S) -> Unit) {
        currentStore.get(block)
    }

    override fun set(stateReducer: S.() -> S) {
        synchronized(lock) {
            val newState = state.stateReducer()
            onStateSetListeners.forEach { it.invoke(state, newState) }

            // Setting state only takes effect is "scriptable" mode is not enabled.
            if (mockBehavior.stateStoreBehavior != StateStoreBehavior.Scriptable) {
                // States are updated on all stores, so if we switch to any other in the future
                // it will be correctly up to date.
                realImmediateStore.set(stateReducer)
                realStore.set(stateReducer)
                scriptableStore.next(newState)
            }
        }
    }

    /**
     * Add a listener that will be called each time a state is set on this state store via
     * the [set] function.
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
