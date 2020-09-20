package com.airbnb.mvrx.mocking

import android.content.Context
import android.content.pm.ApplicationInfo
import com.airbnb.mvrx.Mavericks
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksStateStore
import com.airbnb.mvrx.ScriptableStateStore
import com.airbnb.mvrx.mocking.MockableMavericks.initialize
import com.airbnb.mvrx.mocking.printer.MavericksMockPrinter
import com.airbnb.mvrx.mocking.printer.MockPrinterConfiguration
import com.airbnb.mvrx.mocking.printer.ViewModelStatePrinter

/**
 * Entry point for setting up Mavericks for the app in a mockable way.
 *
 * See [initialize]
 */
object MockableMavericks {
    /**
     * This global instance enables mock states to be forced onto ViewModels as they are created.
     * This enable easy testing.
     *
     * The instance is exposed publicly so references to mocked views can be cleared after a test
     * completes.
     */
    val mockStateHolder = MockStateHolder()

    /**
     * Configuration for how mock state is printed.
     *
     * The Mavericks mocking system allows you to generate a reproduction of a ViewModel's state. For
     * any [MavericksState] instance that a ViewModel has, Mavericks can generate a file containing code
     * to completely reconstruct that state.
     *
     * This generated code can then be used to reconstruct States that can be used during testing.
     * The scripts in the MvRx/mock_generation folder are used to interact with the device to pull
     * the resulting mock files.
     *
     * [enableMockPrinterBroadcastReceiver] must be enabled for this to work.
     *
     * See [MavericksMockPrinter]
     * See https://github.com/airbnb/MvRx/wiki/Mock-Printer
     */
    var mockPrinterConfiguration = MockPrinterConfiguration()

    /**
     * Calls to [MavericksMockPrinter.startReceiver] will be no-ops unless this is enabled.
     *
     * This will automatically be set when [initialize] is called.
     */
    var enableMockPrinterBroadcastReceiver: Boolean = false

    /**
     * Calls to [MockableMavericksView.provideMocks] will return empty unless this is enabled.
     *
     * This will automatically be set when [initialize] is called.
     */
    var enableMavericksViewMocking: Boolean = false

    val mockConfigFactory: MockMavericksViewModelConfigFactory
        get() {
            return (Mavericks.viewModelConfigFactory as? MockMavericksViewModelConfigFactory)
                ?: error("Expecting MockMavericksViewModelConfigFactory for config factory. Make sure you have called MockableMavericks#initialize")
        }

    /**
     * Initializes the required [Mavericks.viewModelConfigFactory] and sets ViewModel debug and mock behavior for the app.
     *
     * If the application was built with the debuggable flag enabled in its Android Manifest then
     * this will add plugins to [Mavericks] that enable working with mock State. This is useful for
     * both manual and automated testing of development builds.
     *
     * This function is a shortcut instead of setting each property in this object individually.
     * For custom control you can set properties directly instead.
     *
     * It is safe to call this in both debug and production
     * builds and it will take care of the correct behavior for you.
     *
     * The context will be used to automatically register a broadcast receiver for each
     * ViewModel created in the app with [ViewModelStatePrinter] so that the state printing
     * system is automatically enabled.
     *
     * Calling this subsequent times will replace the plugins with new instances.
     */
    fun initialize(context: Context) {
        val isDebuggable = context.isDebuggable()
        initialize(
            mocksEnabled = isDebuggable,
            debugMode = isDebuggable,
            context = context
        )
    }

    private fun Context.isDebuggable(): Boolean = (0 != (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE))

    /**
     * Initializes the required [Mavericks.viewModelConfigFactory] and sets ViewModel debug and mock behavior for the app.
     *
     * Choose whether to enable [Mavericks] mocking tools. This is useful for
     * both manual and automated testing of development builds.
     *
     * This function is a shortcut instead of setting each property in this object individually.
     * For custom control you can set properties directly instead.
     *
     * The context will be used to automatically register a broadcast receiver for each
     * ViewModel created in the app with [ViewModelStatePrinter] so that the state printing
     * system is automatically enabled.
     *
     * Calling this subsequent times will replace the plugins with new instances.
     *
     * @param debugMode True if debug checks should be enabled
     * @param mocksEnabled True if ViewModel mocking should be enabled.
     * @param context Application context. If provided this will be used to register a
     * [ViewModelStatePrinter] for each ViewModel to support mock state printing.
     */
    fun initialize(mocksEnabled: Boolean, debugMode: Boolean, context: Context?) {
        enableMockPrinterBroadcastReceiver = mocksEnabled
        enableMavericksViewMocking = mocksEnabled

        if (mocksEnabled) {
            val mockConfigFactory = MockMavericksViewModelConfigFactory(context?.applicationContext, debugMode)
            val mockViewModelDelegateFactory = MockViewModelDelegateFactory(mockConfigFactory)
            Mavericks.initialize(debugMode, mockConfigFactory, mockViewModelDelegateFactory)
        } else {
            Mavericks.initialize(debugMode)
        }
    }

    /**
     * If the given viewmodel has a state store that implements [ScriptableStateStore] then this
     * function can be used to set the next state via [ScriptableStateStore.next].
     *
     * It is an error to call this if the store is not scriptable.
     */
    fun <VM : MavericksViewModel<S>, S : MavericksState> setScriptableState(viewModel: VM, state: S) {
        val stateStore = viewModel.config.stateStore
        check(stateStore is ScriptableStateStore) {
            "State store of ${viewModel.javaClass.simpleName} must be a ScriptableStateStore"
        }
        stateStore.next(state)
    }

    /**
     * A helper to set a state on a view model via [MavericksStateStore.set].
     *
     * This may not work if the ViewModel's state store is mocked or configured to not accept
     * state changes, and it is the responsibility of the caller to make sure that the state store
     * can accept changes.
     *
     * Additionally, it is the responsibility of the caller to understand the type of state store
     * the view model is using, and whether the state change will take affect synchronously or
     * asynchronously depending on the state store implementation.
     *
     * See [setScriptableState] if you want to force a state on a [ScriptableStateStore] that would
     * otherwise not allow state changes.
     */
    fun <VM : MavericksViewModel<S>, S : MavericksState> setState(viewModel: VM, state: S) {
        val stateStore = viewModel.config.stateStore
        stateStore.set { state }
    }
}
