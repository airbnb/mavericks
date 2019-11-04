package com.airbnb.mvrx.mock

import android.content.Context
import android.content.pm.ApplicationInfo
import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.DefaultViewModelDelegateFactory
import com.airbnb.mvrx.MvRx
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.MvRxViewModelConfigFactory
import com.airbnb.mvrx.ScriptableStateStore
import com.airbnb.mvrx.isDebuggable
import com.airbnb.mvrx.mock.printer.MockPrinterConfiguration
import com.airbnb.mvrx.mock.printer.ViewModelStatePrinter

object MvRxMocks {
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
     * The MvRx mocking system allows you to generate a reproduction of a ViewModel's state. For
     * any [MvRxState] instance that a ViewModel has, MvRx can generate a file containing code
     * to completely reconstruct that state.
     *
     * This generated code can then be used to reconstruct States that can be used during testing.
     * The scripts in the MvRx/mock_generation folder are used to interact with the device to pull
     * the resulting mock files.
     *
     * TODO - Link to documentation.
     */
    var mockPrinterConfiguration: MockPrinterConfiguration = MockPrinterConfiguration()

    val mockConfigFactory: MockMvRxViewModelConfigFactory
        get() {
            return (MvRx.viewModelConfigFactory as? MockMvRxViewModelConfigFactory)
                ?: error("Expecting MockMvRxViewModelConfigFactory for config factory. Make sure you have called MvRxMocks#install")
        }

    /**
     * If the application was built with the debuggable flag enabled in its Android Manifest then
     * this will add plugins to [MvRx] that enable working with mock State. This is useful for
     * both manual and automated testing of development builds.
     *
     * If the app is not debuggable then a non debug version of [MvRxViewModelConfigFactory] will be
     * set on [MvRx.viewModelConfigFactory], so it is safe to call this in both debug and production
     * builds and it will take care of the correct behavior for you.
     *
     * The context will be used to automatically register a broadcast receiver for each
     * ViewModel created in the app with [ViewModelStatePrinter] so that the state printing
     * system is automatically enabled.
     *
     * Calling this subsequent times will replace the plugins with new
     * instances.
     */
    fun install(context: Context) {
        installInternal(context, context.isDebuggable)
    }

    internal fun installInternal(context: Context?, isDebuggable: Boolean) {
        if (isDebuggable) {
            val mockConfigFactory = MockMvRxViewModelConfigFactory(context)
            MvRx.viewModelConfigFactory = mockConfigFactory
            MvRx.viewModelDelegateFactory = MockViewModelDelegateFactory(mockConfigFactory)
        } else {
            // These are both set to make sure that all MvRx plugins are completely cleared
            // when debuggable is set to false. This helps in the unit testing case.
            MvRx.viewModelConfigFactory = MvRxViewModelConfigFactory(debugMode = false)
            MvRx.viewModelDelegateFactory = DefaultViewModelDelegateFactory()
        }
    }

    /**
     * If the given viewmodel has a state store that implements [ScriptableStateStore] then this
     * function can be used to set the next state via [ScriptableStateStore.next].
     *
     *
     * It is an error to call this if the store is not scriptable.
     */
    fun <VM : BaseMvRxViewModel<S>, S : MvRxState> setScriptableState(viewModel: VM, state: S) {
        val stateStore = viewModel.config.stateStore
        check(stateStore is ScriptableStateStore) {
            "State store of ${viewModel.javaClass.simpleName} must be a ScriptableStateStore"
        }
        stateStore.next(state)
    }

    fun <VM : BaseMvRxViewModel<S>, S : MvRxState> setState(viewModel: VM, state: S) {
        val stateStore = viewModel.config.stateStore
        check(stateStore is ScriptableStateStore) {
            "State store of ${viewModel.javaClass.simpleName} must be a ScriptableStateStore"
        }
        stateStore.next(state)
    }
}

object MvRxTestMocking {
    /**
     * This is meant to be used in unit test environments where we want to install the mock
     * configs, but don't have a Context so we skip using that to set up the mock printer.
     * The mock printers don't need to be registered anyway for unit tests.
     */
    fun installWithoutMockPrinter(debugMode: Boolean) {
        MvRxMocks.installInternal(context = null, isDebuggable = debugMode)
    }
}