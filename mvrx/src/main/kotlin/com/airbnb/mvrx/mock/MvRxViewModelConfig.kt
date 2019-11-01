package com.airbnb.mvrx.mock


import android.util.Log
import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.MvRx
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.MvRxStateStore
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.RealMvRxStateStore
import java.util.LinkedList

class MvRxViewModelConfig<S : Any>(
    val debugMode: Boolean,
    @PublishedApi internal val stateStore: MvRxStateStore<S>,
    private val initialMockBehavior: MockBehavior? = null
) {
    val currentMockBehavior: MockBehavior?
        get() = mockBehaviorOverrides.peek() ?: initialMockBehavior
    private val mockBehaviorOverrides = LinkedList<MockBehavior>()

    init {
        if (!debugMode && initialMockBehavior != null) {
            Log.e("MvRx", "Mock behavior should only be used in debug", IllegalStateException())
        }
    }

    fun pushBehaviorOverride(mockBehavior: MockBehavior) {
        validateDebug(debugMode) ?: return
        mockBehaviorOverrides.push(mockBehavior)
        updateStateStore()
    }

    private fun updateStateStore() {
        val currentBehavior = currentMockBehavior
        if (stateStore is MockableStateStore && currentBehavior != null) {
            stateStore.mockBehavior = currentBehavior
        }
    }

    fun popBehaviorOverride() {
        validateDebug(debugMode) ?: return
        // It is ok if this list is empty, as the config may have been created after others,
        // so it may not have an override set while other active configs may have one.
        mockBehaviorOverrides.pollFirst()
        updateStateStore()
    }

    companion object {
        /**
         * Allows access to the configuration of a ViewModel.
         * The config is not directly exposed to discourage use. It should only be carefully
         * used in testing frameworks.
         */
        fun <S : MvRxState> access(viewModel: BaseMvRxViewModel<S>): MvRxViewModelConfig<S> {
            return viewModel.config
        }
    }
}

/**
 * Different types of mock setups that can be provided.
 *
 * When a mocked view model is created, "existing" view model expectations are ignored, and the viewmodel is created from
 * mocked state instead of throwing an exception.
 *
 * However, it is up to the caller to make sure that a ViewModel of the expected type doesn't already exist in the view model store, otherwise
 * the framework will use that one instead of allowing us to create a new mocked one, so make sure to clear the store of any previously
 * created viewmodels first.
 */
data class MockBehavior(
    val initialState: InitialState = InitialState.None,
    val blockExecutions: BlockExecutions = BlockExecutions.No,
    val stateStoreBehavior: StateStoreBehavior = StateStoreBehavior.Normal
) {
    /** Describes how a custom mocked state is applied to initialize a new ViewModel. */
    enum class InitialState {
        /** No mocked state is applied. The ViewModel initializes itself through normal means. */
        None,
        /**
         * Generally uses the same behavior as [None], however, if an 'existingViewModel' is accessed and no previous instance exists
         * this will take the default mock state off of the Fragment for that ViewModel and initialize the ViewModel with that state.
         *
         * This is useful when we want a mocked screen to be able to open non mocked screens (in a test), and would otherwise crash
         * if a newly opened screen uses 'existingViewModel' and the viewmodel doesn't actually exist (because we came from a mocked screen
         * that doesn't represent normal screen flow).
         */
        ForceMockExistingViewModel,
        /**
         * Initial view model state is taken from [MockStateHolder] and used in ViewModel creation,
         * but that mocked state may be overridden either in the [MvRxViewModelFactory] or constructor of the ViewModel.
         */
        Partial,
        /**
         * Initial view model state is taken from [MockStateHolder] and used in ViewModel creation.
         * Additionally, any changes to state during ViewModel initialization are forcibly overridden by the mocked state.
         */
        Full
    }

    enum class StateStoreBehavior {
        Normal,
        Scriptable,
        /**
         * When toggled to use the real state store (instead of the scriptable store), this controls whether to use
         * a synchronous version of the state store or the original MvRx state store that operates asynchronously.
         * The immediate, synchronous store can help for testing state changes.
         */
        Synchronous
    }

    enum class BlockExecutions {
        No,
        Completely,
        WithLoading
    }
}

/**
 * Switch between using a mock view model store and a normal view model store.
 *
 * @param debugMode True if this is a debug build of the app, false for production builds.
 * When true,
 */
open class MvRxViewModelConfigProvider(val debugMode: Boolean = true) {

    private val onConfigProvidedListener =
        mutableListOf<(BaseMvRxViewModel<*>, MvRxViewModelConfig<*>) -> Unit>()
    private val mockConfigs = mutableMapOf<MvRxStateStore<*>, MvRxViewModelConfig<*>>()

    /**
     * Determines what sort of mocked state store is created when [provideConfig] is called.
     * This can be changed via [withMockBehavior] to affect behavior when creating a new Fragment.
     *
     * A value can also be set directly here if you want to change the global default.
     *
     * It is only valid to call this when in Debug mode.
     */
    var mockBehavior: MockBehavior? = null
        set(value) {
            field = if (validateDebug(debugMode) == true) value else null
        }

    /**
     * Any view models created within the [block] will be given a viewmodel store that was created according to the rules of the given mock behavior.
     * The default value may have been set by a wrapping call to [withMockBehavior] (ie if multiple calls to withMockBehavior
     * are nested the most recent call can change the behavior of the outer call)
     *
     * After the block has executed, the previous setting for mockBehavior will be used again.
     *
     * @param mockBehavior Null to have ViewModels created with a [RealMvRxStateStore]. Non null to create ViewModels with a [MockableMvRxStateStore]
     * If not null, the [MockableMvRxStateStore] will be created with the options declared in the [MockBehavior]
     */
    fun <R> withMockBehavior(
        mockBehavior: MockBehavior? = this.mockBehavior,
        block: () -> R
    ): R {
        // This function is not inlined so that the caller cannot return early,
        // which would skip setting back the original value!

        // Nesting this call is tricky because an inner call may change the mock behavior that an outer call originally set.
        // To avoid potential bugs because of that we restore the original setting when leaving the block
        val originalSetting = this.mockBehavior

        this.mockBehavior = mockBehavior
        val result = block()
        this.mockBehavior = originalSetting

        return result
    }

    private fun onMockStoreDisposed(store: MockableStateStore<*>) {
        mockConfigs.remove(store)
    }

    fun <S : MvRxState> provideConfig(
        viewModel: BaseMvRxViewModel<S>,
        initialState: S
    ): MvRxViewModelConfig<S> {
        val mockBehavior = mockBehavior

        val stateStore = buildStateStore(initialState, mockBehavior)

        return MvRxViewModelConfig(
            debugMode,
            stateStore,
            mockBehavior
        ).also { config ->
            if (mockBehavior != null && stateStore is MockableMvRxStateStore) {
                mockConfigs[stateStore] = config
                stateStore.addOnDisposeListener(::onMockStoreDisposed)
            }
            onConfigProvidedListener.forEach { callback -> callback(viewModel, config) }
        }
    }

    open fun <S : Any> buildStateStore(
        initialState: S,
        mockBehavior: MockBehavior?
    ): MvRxStateStore<S> {
        return if (mockBehavior != null && debugMode) {
            MockableMvRxStateStore(
                initialState = initialState,
                mockBehavior = mockBehavior
            )
        } else {
            RealMvRxStateStore(initialState)
        }
    }

    /**
     * Add a listener that will be called every time a [MvRxViewModelConfig] is created for a new
     * view model. This will happen each time a new ViewModel is created.
     *
     * The callback includes a reference to the ViewModel that the config was created for, as well
     * as the configuration itself.
     */
    fun addOnConfigProvidedListener(callback: (BaseMvRxViewModel<*>, MvRxViewModelConfig<*>) -> Unit) {
        onConfigProvidedListener.add(callback)
    }

    fun removeOnConfigProvidedListener(callback: (BaseMvRxViewModel<*>, MvRxViewModelConfig<*>) -> Unit) {
        onConfigProvidedListener.remove(callback)
    }

    /**
     * Changes the current [MockBehavior] of all running ViewModels, if they were created when
     * [mockBehavior] was non null. This forces the mock behavior of to this new value.
     *
     * This should be followed later by a corresponding call to [popMockBehaviorOverride] in order
     * to revert the mock behavior to its original value.
     */
    fun pushMockBehaviorOverride(mockBehavior: MockBehavior) {
        validateDebug(debugMode) ?: return
        mockConfigs.values.forEach { it.pushBehaviorOverride(mockBehavior) }
    }

    fun popMockBehaviorOverride() {
        validateDebug(debugMode) ?: return
        mockConfigs.values.forEach { it.popBehaviorOverride() }
    }
}

internal fun validateDebug(debug: Boolean = MvRx.viewModelConfigProvider.debugMode): Boolean? {
    return if (debug) {
        true
    } else {
        // Not using Log.e to avoid needing robolectric in tests.
        System.err.println("MvRxViewModelConfigProvider: This is only accessible in debug mode")
        null
    }
}
