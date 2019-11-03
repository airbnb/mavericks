package com.airbnb.mvrx.mock

import android.content.Context
import android.content.pm.ApplicationInfo
import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.MvRx
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.MvRxViewModelConfigFactory
import com.airbnb.mvrx.ScriptableStateStore
import com.airbnb.mvrx.mock.printer.MockPrinterConfiguration

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

    /**
     * If the application was built with the debuggable flag enabled in its Android Manifest then
     * this will add plugins to [MvRx] that enable working with mock State. This is useful for
     * both manual and automated testing of development builds.
     *
     * If the app is not debuggable then a non debug version of [MvRxViewModelConfigFactory] will be
     * set on [MvRx.viewModelConfigFactory].
     */
    fun install(context: Context) {
        val isDebuggable = 0 != (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE)

        if (isDebuggable) {
            val mockConfigFactory = MockMvRxViewModelConfigFactory(context)
            MvRx.viewModelConfigFactory = mockConfigFactory
            MvRx.viewModelDelegateFactory = MockViewModelDelegateFactory(mockConfigFactory)
        } else {
            MvRx.viewModelConfigFactory = MvRxViewModelConfigFactory(debugMode = false)
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
}