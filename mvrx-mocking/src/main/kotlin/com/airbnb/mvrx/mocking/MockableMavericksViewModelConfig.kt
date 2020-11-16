package com.airbnb.mvrx.mocking

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.airbnb.mvrx.CoroutinesStateStore
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelConfig
import com.airbnb.mvrx.MavericksViewModelConfigFactory
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksStateStore
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ScriptableStateStore
import com.airbnb.mvrx.mocking.printer.ViewModelStatePrinter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.LinkedList
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class MockableMavericksViewModelConfig<S : MavericksState>(
    val mockableStateStore: MockableMavericksStateStore<S>,
    val initialMockBehavior: MockBehavior,
    coroutineScope: CoroutineScope,
    debugMode: Boolean
) : MavericksViewModelConfig<S>(debugMode = debugMode, stateStore = mockableStateStore, coroutineScope = coroutineScope) {

    val currentMockBehavior: MockBehavior
        get() = mockBehaviorOverrides.peek() ?: initialMockBehavior

    private val mockBehaviorOverrides = LinkedList<MockBehavior>()

    private val onExecuteListeners =
        mutableSetOf<(MavericksViewModelConfig<*>, MavericksViewModel<*>, MavericksViewModelConfig.BlockExecutions) -> Unit>()

    fun pushBehaviorOverride(behaviorChange: (currentBehavior: MockBehavior) -> MockBehavior) {
        pushBehaviorOverride(behaviorChange(currentMockBehavior))
    }

    fun pushBehaviorOverride(mockBehavior: MockBehavior) {
        mockBehaviorOverrides.push(mockBehavior)
        updateStateStore()
    }

    private fun updateStateStore() {
        mockableStateStore.mockBehavior = currentMockBehavior
    }

    fun popBehaviorOverride() {
        // It is ok if this list is empty, as the config may have been created after others,
        // so it may not have an override set while other active configs may have one.
        mockBehaviorOverrides.pollFirst()
        updateStateStore()
    }

    fun addOnExecuteListener(listener: (MavericksViewModelConfig<*>, MavericksViewModel<*>, MavericksViewModelConfig.BlockExecutions) -> Unit) {
        onExecuteListeners.add(listener)
    }

    fun removeOnExecuteListener(listener: (MavericksViewModelConfig<*>, MavericksViewModel<*>, MavericksViewModelConfig.BlockExecutions) -> Unit) {
        onExecuteListeners.remove(listener)
    }

    fun clearOnExecuteListeners(): Unit = onExecuteListeners.clear()

    override fun <S : MavericksState> onExecute(viewModel: MavericksViewModel<S>): BlockExecutions {
        val blockExecutions = currentMockBehavior.blockExecutions

        onExecuteListeners.forEach {
            it(this, viewModel, blockExecutions)
        }

        return blockExecutions
    }

    companion object {
        /**
         * Allows access to the mock configuration of a ViewModel.
         *
         * The config is not directly exposed to discourage use. It should only be carefully
         * used in testing frameworks.
         *
         * This assumes the viewmodel was created with a mock config applied, and fails otherwise.
         */
        fun <S : MavericksState> access(viewModel: MavericksViewModel<S>): MockableMavericksViewModelConfig<S> {
            return viewModel.config as MockableMavericksViewModelConfig
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
    val initialStateMocking: InitialStateMocking = InitialStateMocking.None,
    val blockExecutions: MavericksViewModelConfig.BlockExecutions = MavericksViewModelConfig.BlockExecutions.No,
    val stateStoreBehavior: StateStoreBehavior = StateStoreBehavior.Normal,
    /**
     * If true, when a view registers a ViewModel via a delegate the view will be subscribed
     * to changes to the state view [MavericksView.postInvalidate]. This is the normal behavior of
     * MvRx. This can be set to false so that a Fragment is not updated for state changes during
     * tests.
     */
    val subscribeViewToStateUpdates: Boolean = true
) {
    /** Describes how a custom mocked state is applied to initialize a new ViewModel. */
    enum class InitialStateMocking {
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
         * but that mocked state may be overridden either in the [MavericksViewModelFactory] or constructor of the ViewModel.
         */
        Partial,

        /**
         * Initial view model state is taken from [MockStateHolder] and used in ViewModel creation.
         * Additionally, any changes to state during ViewModel initialization are forcibly overridden by the mocked state.
         */
        Full
    }

    enum class StateStoreBehavior {
        /**
         * Uses RealMavericksStateStore.
         */
        Normal,

        /**
         * An implementation of [ScriptableStateStore] that blocks any calls to [MavericksStateStore.set],
         * and instead allows immediate state updates via [ScriptableStateStore.next].
         */
        Scriptable,

        /**
         * A fully functional [MavericksStateStore] implementation that makes all updates synchronously.
         */
        Synchronous
    }
}

open class MockMavericksViewModelConfigFactory(
    /**
     * The application context. If provided this will be used to register a
     * [ViewModelStatePrinter] for each ViewModel to enable mock state printing.
     */
    applicationContext: Context?,
    debugMode: Boolean = true,
    /**
     * Provide a default coroutine context for viewModelScope in all view models. It will be added after [SupervisorJob]
     * and [Dispatchers.Main.immediate].
     */
    viewModelCoroutineContext: CoroutineContext = EmptyCoroutineContext,
    /**
     * Provide a coroutine context that will be used in the [CoroutinesStateStore]. All withState/setState calls will be executed in this context.
     */
    stateStoreCoroutineContext: CoroutineContext = EmptyCoroutineContext,
    /**
     * Provide a context that will be added to the coroutine scope when a subscription is registered (eg [MavericksView.onEach]).
     *
     * By default subscriptions use [MavericksView.subscriptionLifecycleOwner] and [LifecycleOwner.lifecycleScope] to
     * retrieve a coroutine scope to launch the subscription in.
     */
    subscriptionCoroutineContextOverride: CoroutineContext = EmptyCoroutineContext,
) : MavericksViewModelConfigFactory(debugMode, viewModelCoroutineContext, stateStoreCoroutineContext, subscriptionCoroutineContextOverride) {

    private val applicationContext: Context? = applicationContext?.applicationContext

    private val mockConfigs = mutableMapOf<MavericksStateStore<*>, MockableMavericksViewModelConfig<*>>()

    val currentConfigs: Map<MavericksStateStore<*>, MockableMavericksViewModelConfig<*>>
        get() = mockConfigs.toMap()

    private val onExecuteListeners =
        mutableSetOf<(MavericksViewModelConfig<*>, MavericksViewModel<*>, MavericksViewModelConfig.BlockExecutions) -> Unit>()

    /**
     * Determines what sort of mocked state store is created when [provideConfig] is called.
     * This can be changed via [withMockBehavior] to affect behavior when creating a new Fragment.
     *
     * A value can also be set directly here if you want to change the global default.
     */
    var mockBehavior: MockBehavior = MockBehavior(
        initialStateMocking = MockBehavior.InitialStateMocking.None,
        blockExecutions = MavericksViewModelConfig.BlockExecutions.No,
        stateStoreBehavior = MockBehavior.StateStoreBehavior.Normal
    )

    /**
     * Any view models created within the [block] will be given a viewmodel store that was created according to the rules of the given mock behavior.
     * The default value may have been set by a wrapping call to [withMockBehavior] (ie if multiple calls to withMockBehavior
     * are nested the most recent call can change the behavior of the outer call)
     *
     * After the block has executed, the previous setting for mockBehavior will be used again.
     *
     * @param mockBehavior Null to have ViewModels created with a [RealMvRxStateFactory]. Non null to create ViewModels with a [MockableMavericksStateStore]
     * If not null, the [MockableMavericksStateStore] will be created with the options declared in the [MockBehavior]
     */
    fun <R> withMockBehavior(
        mockBehavior: MockBehavior = this.mockBehavior,
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

    override fun <S : MavericksState> buildConfig(viewModel: MavericksViewModel<S>, initialState: S): MavericksViewModelConfig<S> {
        val mockBehavior = mockBehavior
        val coroutineScope = coroutineScope()

        val stateStore = MockableMavericksStateStore(
            initialState = initialState,
            mockBehavior = mockBehavior,
            coroutineScope = coroutineScope,
            contextOverride = contextOverride
        )

        return MockableMavericksViewModelConfig(
            mockableStateStore = stateStore,
            initialMockBehavior = mockBehavior,
            debugMode = debugMode,
            coroutineScope = coroutineScope
        ).also { config ->
            // Since this is an easy place to hook into all viewmodel creation and clearing
            // we use it as an opportunity to register the mock printer on all view models.
            // This lets us capture singleton viewmodels as well.
            val viewModelStatePrinter = ViewModelStatePrinter(viewModel)
            val enableMockPrinterBroadcastReceiver = MockableMavericks.enableMockPrinterBroadcastReceiver
            applicationContext?.let { context ->
                if (enableMockPrinterBroadcastReceiver) {
                    viewModelStatePrinter.register(context)
                }
            }

            onExecuteListeners.forEach { config.addOnExecuteListener(it) }

            mockConfigs[stateStore] = config
            stateStore.addOnCancelListener { stateStore ->
                applicationContext?.let { context ->
                    if (enableMockPrinterBroadcastReceiver) {
                        viewModelStatePrinter.unregister(context)
                    }
                }
                onMockStoreDisposed(stateStore)
            }
        }
    }

    /**
     * Changes the current [MockBehavior] of all running ViewModels, if they were created when
     * [mockBehavior] was non null. This forces the mock behavior to this new value.
     *
     * This should be followed later by a corresponding call to [popMockBehaviorOverride] in order
     * to revert the mock behavior to its original value.
     */
    fun pushMockBehaviorOverride(mockBehavior: MockBehavior) {
        mockConfigs.values.forEach { it.pushBehaviorOverride(mockBehavior) }
    }

    fun pushMockBehaviorOverride(behaviorChange: (currentBehavior: MockBehavior) -> MockBehavior) {
        mockConfigs.values.forEach { it.pushBehaviorOverride(behaviorChange(it.currentMockBehavior)) }
    }

    fun popMockBehaviorOverride() {
        mockConfigs.values.forEach { it.popBehaviorOverride() }
    }

    /**
     * Add a lambda that will be invoked whenever [MavericksViewModel.execute] is used.
     */
    fun addOnExecuteListener(listener: (MavericksViewModelConfig<*>, MavericksViewModel<*>, MavericksViewModelConfig.BlockExecutions) -> Unit) {
        onExecuteListeners.add(listener)
        mockConfigs.values.forEach { it.addOnExecuteListener(listener) }
    }

    fun removeOnExecuteListener(listener: (MavericksViewModelConfig<*>, MavericksViewModel<*>, MavericksViewModelConfig.BlockExecutions) -> Unit) {
        onExecuteListeners.remove(listener)
        mockConfigs.values.forEach { it.removeOnExecuteListener(listener) }
    }
}
