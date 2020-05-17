package com.airbnb.mvrx

import androidx.annotation.CallSuper
import androidx.annotation.RestrictTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

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
    protected fun withState(block: (state: S) -> Unit) {
        stateStore.get(block)
    }

    private operator fun CoroutineContext.plus(other: CoroutineContext?) = if (other == null) this else this + other

    override fun toString(): String = "${this::class.java.simpleName} $state"
}

