package com.airbnb.mvrx.mocking

import com.airbnb.mvrx.Mavericks
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelConfig
import com.airbnb.mvrx.MavericksViewModelConfigFactory
import com.airbnb.mvrx.MavericksState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MavericksViewModelConfigTest : BaseTest() {

    @Test
    fun mockBehaviorIsConfigurableInBlock() {
        val provider = MockMavericksViewModelConfigFactory(applicationContext = null)

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

        Mavericks.viewModelConfigFactory = MavericksViewModelConfigFactory(debugMode = false)
        val vm2 = TestViewModel()
        assertFalse(vm2.config.debugMode)
    }

    @Test
    fun testAddOnConfigProvidedListener() {
        var providedVm: MavericksViewModel<*>? = null
        var providedConfig: MavericksViewModelConfig<*>? = null
        val onConfigProvided = { vm: MavericksViewModel<*>, config: MavericksViewModelConfig<*> ->
            providedVm = vm
            providedConfig = config
        }

        Mavericks.viewModelConfigFactory.addOnConfigProvidedListener(onConfigProvided)
        val vm = TestViewModel()

        assertEquals(vm, providedVm)
        assertEquals(true, providedConfig?.debugMode)
        assertEquals(vm.config.stateStore, providedConfig?.stateStore)
    }

    @Test
    fun testRemoveOnConfigProvidedListener() {
        var providedVm: MavericksViewModel<*>? = null
        var providedConfig: MavericksViewModelConfig<*>? = null
        val onConfigProvided = { vm: MavericksViewModel<*>, config: MavericksViewModelConfig<*> ->
            providedVm = vm
            providedConfig = config
        }

        Mavericks.viewModelConfigFactory.addOnConfigProvidedListener(onConfigProvided)
        Mavericks.viewModelConfigFactory.removeOnConfigProvidedListener(onConfigProvided)
        TestViewModel()

        assertNull(providedConfig)
        assertNull(providedVm)
    }

    @Test
    fun pushMockBehaviorOverride() {
        val originalBehavior = MockableMavericks.mockConfigFactory.mockBehavior
        val vm = TestViewModel()

        val newBehavior =
            originalBehavior.copy(stateStoreBehavior = MockBehavior.StateStoreBehavior.Synchronous)

        MockableMavericks.mockConfigFactory.pushMockBehaviorOverride(newBehavior)

        assertEquals(
            newBehavior,
            (vm.config as MockableMavericksViewModelConfig).currentMockBehavior
        )

        assertEquals(
            newBehavior,
            (vm.config.stateStore as MockableStateStore).mockBehavior
        )

        MockableMavericks.mockConfigFactory.popMockBehaviorOverride()

        assertEquals(
            originalBehavior,
            vm.config.currentMockBehavior
        )

        assertEquals(
            originalBehavior,
            (vm.config.stateStore as MockableStateStore).mockBehavior
        )
    }

    @Test
    fun pushMockBehaviorOverrideTransform() {
        val originalBehavior = MockableMavericks.mockConfigFactory.mockBehavior
        val vm = TestViewModel()

        MockableMavericks.mockConfigFactory.pushMockBehaviorOverride { currentBehavior ->
            currentBehavior.copy(stateStoreBehavior = MockBehavior.StateStoreBehavior.Synchronous)
        }

        assertEquals(
            originalBehavior.copy(stateStoreBehavior = MockBehavior.StateStoreBehavior.Synchronous),
            (vm.config.stateStore as MockableStateStore).mockBehavior
        )

        MockableMavericks.mockConfigFactory.popMockBehaviorOverride()

        assertEquals(
            originalBehavior,
            (vm.config.stateStore as MockableStateStore).mockBehavior
        )
    }

    class TestViewModel : MavericksViewModel<TestState>(TestState())

    data class TestState(val num: Int = 0) : MavericksState
}