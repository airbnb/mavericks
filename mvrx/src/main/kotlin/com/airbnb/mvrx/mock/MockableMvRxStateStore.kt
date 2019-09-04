package com.airbnb.mvrx.mock

import com.airbnb.mvrx.MvRxStateStore
import com.airbnb.mvrx.RealMvRxStateStore
import com.airbnb.mvrx.ScriptableMvRxStateStore
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

interface MockableStateStore<S : Any> : MvRxStateStore<S> {
    var mockBehavior: MockBehavior
}

/**
 * Allows switching between a mocked state value and a real state store behavior.
 */
class MockableMvRxStateStore<S : Any>(
    initialState: S,
    override var mockBehavior: MockBehavior,
    val onDisposed: (MockableMvRxStateStore<*>) -> Unit
) : MockableStateStore<S> {
    private val scriptableStore = ScriptableMvRxStateStore(initialState)
    private val realStore = RealMvRxStateStore(initialState)
    private val realImmediateStore = ImmediateRealMvRxStateStore(initialState)

    private val onStateSetListeners = mutableListOf<(previousState: S, newState: S) -> Unit>()
    private val onDisposeListeners = mutableListOf<() -> Unit>()

    private val currentStore
        get() = when (mockBehavior.stateStoreBehavior) {
            MockBehavior.StateStoreBehavior.Scriptable -> scriptableStore
            MockBehavior.StateStoreBehavior.Normal -> realStore
            MockBehavior.StateStoreBehavior.Synchronous -> realImmediateStore
        }

    // Using "trampoline" scheduler so that updates are processed synchronously.
    // This allows changes to be dispatched in a more controllable way for testing,
    // to reduce flakiness in accounting for changes in screenshot tests.
    override val observable: Observable<S>
        get() = currentStore.observable.observeOn(Schedulers.trampoline())

    override val state: S
        get() = currentStore.state

    override fun dispose() {
        realStore.dispose()
        realImmediateStore.dispose()
        scriptableStore.dispose()
        onDisposed(this)
        onDisposeListeners.forEach { it() }
        onDisposeListeners.clear()
    }

    fun next(state: S) {
        require(mockBehavior.stateStoreBehavior == MockBehavior.StateStoreBehavior.Scriptable) {
            "Scriptable store is not enabled"
        }

        scriptableStore.next(state)
        // Update the real stores too, so if we switch to them they are already up to date.
        realStore.set { state }
        realImmediateStore.set { state }
    }

    override fun get(block: (S) -> Unit) {
        currentStore.get(block)
    }

    override fun isDisposed(): Boolean {
        return realStore.isDisposed
    }

    override fun set(stateReducer: S.() -> S) {
        if (onStateSetListeners.isNotEmpty()) {
            val newState = state.stateReducer()
            onStateSetListeners.forEach { it.invoke(state, newState) }
        }

        if (mockBehavior.stateStoreBehavior != MockBehavior.StateStoreBehavior.Scriptable) {
            realImmediateStore.set(stateReducer)
            realStore.set(stateReducer)
        }
    }

    fun addOnStateSetListener(callback: (previousState: S, newState: S) -> Unit) {
        onStateSetListeners.add(callback)
    }

    fun removeOnStateSetListener(callback: (previousState: S, newState: S) -> Unit) {
        onStateSetListeners.remove(callback)
    }

    fun addOnDisposeListener(callback: () -> Unit) {
        if (isDisposed) {
            callback()
        } else {
            onDisposeListeners.add(callback)
        }
    }
}
