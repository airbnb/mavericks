package com.airbnb.mvrx.mock

import android.util.Log
import androidx.annotation.VisibleForTesting
import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.MvRxState
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
    var mockPrinterConfiguration: MockPrinterConfiguration =
        MockPrinterConfiguration()

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