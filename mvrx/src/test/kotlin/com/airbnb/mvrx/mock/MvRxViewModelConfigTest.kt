package com.airbnb.mvrx.mock

import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.BaseTest
import com.airbnb.mvrx.MvRx
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.MvRxViewModelConfig
import com.airbnb.mvrx.MvRxViewModelConfigFactory
import com.airbnb.mvrx.RealMvRxStateStore
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MvRxViewModelConfigTest : BaseTest() {


    @Test
    fun mockBehaviorIsConfigurableInBlock() {
        val provider = MockMvRxViewModelConfigFactory(context = null)

        val originalBehavior = provider.mockBehavior
        val newBehavior =
            MockBehavior(blockExecutions = MvRxViewModelConfig.BlockExecutions.Completely)
        val newBehavior2 =
            MockBehavior(blockExecutions = MvRxViewModelConfig.BlockExecutions.WithLoading)

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
        assertEquals(provider.mockBehavior, originalBehavior)
    }

    @Test
    fun testViewModelDebugModeControlledByProvider() {
        // Default debug mode is true
        val vm = TestViewModel()
        assertTrue(vm.config.debugMode)

        MvRx.viewModelConfigFactory = MvRxViewModelConfigFactory(debugMode = false)
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

        MvRx.nonNullViewModelConfigFactory.addOnConfigProvidedListener(onConfigProvided)
        val vm = TestViewModel()

        assertEquals(vm, providedVm)
        assertEquals(true, providedConfig?.debugMode)
        assertEquals(vm.config.stateStore, providedConfig?.stateStore)
    }

    @Test
    fun testRemoveOnConfigProvidedListener() {
        var providedVm: BaseMvRxViewModel<*>? = null
        var providedConfig: MvRxViewModelConfig<*>? = null
        val onConfigProvided = { vm: BaseMvRxViewModel<*>, config: MvRxViewModelConfig<*> ->
            providedVm = vm
            providedConfig = config
        }

        MvRx.nonNullViewModelConfigFactory.addOnConfigProvidedListener(onConfigProvided)
        MvRx.nonNullViewModelConfigFactory.removeOnConfigProvidedListener(onConfigProvided)
        TestViewModel()

        assertNull(providedConfig)
        assertNull(providedVm)
    }

    @Test
    fun pushMockBehaviorOverride() {
        val originalBehavior = MvRxMocks.mockConfigFactory.mockBehavior

        val vm = TestViewModel()

        val newBehavior =
            originalBehavior.copy(stateStoreBehavior = MockBehavior.StateStoreBehavior.Synchronous)

        MvRxMocks.mockConfigFactory.pushMockBehaviorOverride(newBehavior)

        assertEquals(
            newBehavior,
            (vm.config as MockableMvRxViewModelConfig).currentMockBehavior
        )

        assertEquals(
            newBehavior,
            (vm.config.stateStore as MockableStateStore).mockBehavior
        )

        MvRxMocks.mockConfigFactory.popMockBehaviorOverride()

        assertEquals(
            originalBehavior,
            vm.config.currentMockBehavior
        )

        assertEquals(
            originalBehavior,
            vm.config.stateStore.mockBehavior
        )
    }

    class TestViewModel : BaseMvRxViewModel<TestState>(TestState())

    data class TestState(val num: Int = 0) : MvRxState
}