package com.airbnb.mvrx.mocking

import com.airbnb.mvrx.BaseMavericksViewModel
import com.airbnb.mvrx.MvRx
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.MavericksViewModelConfig
import com.airbnb.mvrx.MavericksViewModelConfigFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MavericksViewModelConfigTest : BaseTest() {


    @Test
    fun mockBehaviorIsConfigurableInBlock() {
        val provider = MockMavericksViewModelConfigFactory(context = null)

        val originalBehavior = provider.mockBehavior
        val newBehavior =
                MockBehavior(blockExecutions = MavericksViewModelConfig.BlockExecutions.Completely)
        val newBehavior2 =
                MockBehavior(blockExecutions = MavericksViewModelConfig.BlockExecutions.WithLoading)

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

        MvRx.viewModelConfigFactory = MavericksViewModelConfigFactory(debugMode = false)
        val vm2 = TestViewModel()
        assertFalse(vm2.config.debugMode)
    }

    @Test
    fun testAddOnConfigProvidedListener() {
        var providedVm: BaseMavericksViewModel<*>? = null
        var providedConfig: MavericksViewModelConfig<*>? = null
        val onConfigProvided = { vm: BaseMavericksViewModel<*>, config: MavericksViewModelConfig<*> ->
            providedVm = vm
            providedConfig = config
        }

        MvRx.viewModelConfigFactory!!.addOnConfigProvidedListener(onConfigProvided)
        val vm = TestViewModel()

        assertEquals(vm, providedVm)
        assertEquals(true, providedConfig?.debugMode)
        assertEquals(vm.config.stateStore, providedConfig?.stateStore)
    }

    @Test
    fun testRemoveOnConfigProvidedListener() {
        var providedVm: BaseMavericksViewModel<*>? = null
        var providedConfig: MavericksViewModelConfig<*>? = null
        val onConfigProvided = { vm: BaseMavericksViewModel<*>, config: MavericksViewModelConfig<*> ->
            providedVm = vm
            providedConfig = config
        }

        MvRx.viewModelConfigFactory!!.addOnConfigProvidedListener(onConfigProvided)
        MvRx.viewModelConfigFactory!!.removeOnConfigProvidedListener(onConfigProvided)
        TestViewModel()

        assertNull(providedConfig)
        assertNull(providedVm)
    }

    @Test
    fun pushMockBehaviorOverride() {
        val originalBehavior = MavericksMocks.mockConfigFactory.mockBehavior
        val vm = TestViewModel()

        val newBehavior =
                originalBehavior.copy(stateStoreBehavior = MockBehavior.StateStoreBehavior.Synchronous)

        MavericksMocks.mockConfigFactory.pushMockBehaviorOverride(newBehavior)

        assertEquals(
                newBehavior,
                (vm.config as MockableMavericksViewModelConfig).currentMockBehavior
        )

        assertEquals(
                newBehavior,
                (vm.config.stateStore as MockableStateStore).mockBehavior
        )

        MavericksMocks.mockConfigFactory.popMockBehaviorOverride()

        assertEquals(
                originalBehavior,
                vm.config.currentMockBehavior
        )

        assertEquals(
                originalBehavior,
                (vm.config.stateStore as MockableStateStore).mockBehavior
        )
    }

    class TestViewModel : BaseMavericksViewModel<TestState>(TestState())

    data class TestState(val num: Int = 0) : MvRxState
}