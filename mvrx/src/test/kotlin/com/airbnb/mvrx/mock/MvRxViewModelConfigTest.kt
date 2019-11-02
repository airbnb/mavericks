package com.airbnb.mvrx.mock

import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.BaseTest
import com.airbnb.mvrx.MvRx
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.MvRxViewModelConfigProvider
import com.airbnb.mvrx.RealMvRxStateStore
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MvRxViewModelConfigTest : BaseTest() {
    @Before
    fun resetProvider() {
        MvRx.viewModelConfigProvider =
            MvRxViewModelConfigProvider()
    }

    @Test
    fun mockBehaviorIsConfigurableViaProperty() {
        val provider = MvRxViewModelConfigProvider()
        assertNull(provider.mockBehavior)

        val newBehavior = MockBehavior(
            MockBehavior.InitialState.Full,
            MockBehavior.BlockExecutions.Completely,
            MockBehavior.StateStoreBehavior.Scriptable
        )

        provider.mockBehavior = newBehavior

        assertEquals(newBehavior, provider.mockBehavior)
    }

    @Test
    fun nullMockBehaviorWhenNotDebugMode() {
        val provider =
            MvRxViewModelConfigProvider(debugMode = false)

        provider.mockBehavior = MockBehavior(
            MockBehavior.InitialState.Full,
            MockBehavior.BlockExecutions.Completely,
            MockBehavior.StateStoreBehavior.Scriptable
        )

        assertNull(provider.mockBehavior)
    }

    @Test
    fun mockBehaviorIsConfigurableInBlock() {
        val provider = MvRxViewModelConfigProvider()

        val newBehavior = MockBehavior(
            MockBehavior.InitialState.Full,
            MockBehavior.BlockExecutions.Completely,
            MockBehavior.StateStoreBehavior.Scriptable
        )

        val newBehavior2 = MockBehavior(
            MockBehavior.InitialState.Full,
            MockBehavior.BlockExecutions.Completely,
            MockBehavior.StateStoreBehavior.Normal
        )

        val result = provider.withMockBehavior(newBehavior) {
            assertEquals(newBehavior, provider.mockBehavior)

            // Nesting calls continues to change behavior
            provider.withMockBehavior(newBehavior2) {
                assertEquals(newBehavior2, provider.mockBehavior)
            }

            // Behavior is restored after a nested call
            assertEquals(newBehavior, provider.mockBehavior)
            5
        }

        // The result of the lambda is correctly returned
        assertEquals(5, result)

        // Behavior gets set back to the original
        assertNull(provider.mockBehavior)
    }

    @Test
    fun testViewModelDebugModeControlledByProvider() {
        // Default debug mode is true
        val vm = TestViewModel()
        assertTrue(vm.config.debugMode)

        MvRx.viewModelConfigProvider =
            MvRxViewModelConfigProvider(debugMode = false)
        val vm2 = TestViewModel()
        assertFalse(vm2.config.debugMode)
    }

    @Test
    fun testAddOnConfigProvidedListener() {
        var providedVm: BaseMvRxViewModel<*>? = null
        var providedConfig: MvRxViewModelConfig<*>? = null
        val onConfigProvided = { vm: BaseMvRxViewModel<*>, config: MvRxViewModelConfig<*> ->
            providedVm = vm
            providedConfig = config
        }

        MvRx.viewModelConfigProvider.addOnConfigProvidedListener(onConfigProvided)
        val vm = TestViewModel()

        assertEquals(vm, providedVm)
        assertEquals(true, providedConfig?.debugMode)
        assertEquals(vm.config.stateStore, providedConfig?.stateStore)
        assertNull(providedConfig?.currentMockBehavior)
    }

    @Test
    fun testRemoveOnConfigProvidedListener() {
        var providedVm: BaseMvRxViewModel<*>? = null
        var providedConfig: MvRxViewModelConfig<*>? = null
        val onConfigProvided = { vm: BaseMvRxViewModel<*>, config: MvRxViewModelConfig<*> ->
            providedVm = vm
            providedConfig = config
        }

        MvRx.viewModelConfigProvider.addOnConfigProvidedListener(onConfigProvided)
        MvRx.viewModelConfigProvider.removeOnConfigProvidedListener(onConfigProvided)
        TestViewModel()

        assertNull(providedConfig)
        assertNull(providedVm)
    }

    @Test
    fun testNoMockBehaviorByDefault() {
        val vm = TestViewModel()
        assertTrue(vm.config.stateStore is RealMvRxStateStore)
    }

    @Test
    fun mockableMvRxStateStoreIsUsedWhenMockBehaviorIsSet() {
        MvRx.viewModelConfigProvider.mockBehavior = MockBehavior(
            MockBehavior.InitialState.Full,
            MockBehavior.BlockExecutions.Completely,
            MockBehavior.StateStoreBehavior.Scriptable
        )

        val vm = TestViewModel()
        val stateStore = vm.config.stateStore
        check(stateStore is MockableMvRxStateStore)

        // The store behavior should be what was set in the mock behavior
        assertEquals(
            MockBehavior.StateStoreBehavior.Scriptable,
            stateStore.mockBehavior.stateStoreBehavior
        )
        assertEquals(MockBehavior.StateStoreBehavior.Scriptable, vm.config.currentMockBehavior?.stateStoreBehavior)
    }

    @Test
    fun pushMockBehaviorOverride() {
        val originalBehavior = MockBehavior(
            MockBehavior.InitialState.Full,
            MockBehavior.BlockExecutions.Completely,
            MockBehavior.StateStoreBehavior.Scriptable
        )
        MvRx.viewModelConfigProvider.mockBehavior = originalBehavior

        val vm = TestViewModel()

        val newBehavior =
            originalBehavior.copy(stateStoreBehavior = MockBehavior.StateStoreBehavior.Synchronous)
        MvRx.viewModelConfigProvider.pushMockBehaviorOverride(newBehavior)

        assertEquals(
            newBehavior,
            vm.config.currentMockBehavior
        )

        assertEquals(
            newBehavior,
            (vm.config.stateStore as MockableStateStore).mockBehavior
        )

        MvRx.viewModelConfigProvider.popMockBehaviorOverride()

        assertEquals(
            originalBehavior,
            vm.config.currentMockBehavior
        )

        assertEquals(
            originalBehavior,
            (vm.config.stateStore as MockableStateStore).mockBehavior
        )
    }

    class TestViewModel : BaseMvRxViewModel<TestState>(TestState())

    data class TestState(val num: Int = 0) : MvRxState
}