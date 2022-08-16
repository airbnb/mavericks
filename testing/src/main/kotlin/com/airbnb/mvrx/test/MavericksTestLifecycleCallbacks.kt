package com.airbnb.mvrx.test

import com.airbnb.mvrx.DefaultViewModelDelegateFactory
import com.airbnb.mvrx.Mavericks
import com.airbnb.mvrx.MavericksViewModelConfigFactory
import com.airbnb.mvrx.MvRxTestOverridesProxy
import com.airbnb.mvrx.mocking.MockBehavior
import com.airbnb.mvrx.mocking.MockableMavericks
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

internal interface MavericksTestLifecycleCallbacks {
    fun before()
    fun after()
}

/**
 * Utility class that wraps common test code for both
 * the JUnit 4 Rule & JUnit 5 Extension.
 */
internal class MavericksTestLifecycleCallbacksImpl(
    private val setForceDisableLifecycleAwareObserver: Boolean = true,
    private val viewModelMockBehavior: MockBehavior? = MockBehavior(
        stateStoreBehavior = MockBehavior.StateStoreBehavior.Synchronous
    ),
    private val debugMode: Boolean = false,
    @Suppress("EXPERIMENTAL_API_USAGE")
    private val testDispatcher: CoroutineDispatcher = StandardTestDispatcher()
) : MavericksTestLifecycleCallbacks {

    override fun before() {
        Dispatchers.setMain(testDispatcher)

        MvRxTestOverridesProxy.forceDisableLifecycleAwareObserver(setForceDisableLifecycleAwareObserver)

        setupMocking()
    }

    override fun after() {
        Dispatchers.resetMain()

        // clear any changes or listeners that were set on the plugins, and reset defaults
        MockableMavericks.enableMavericksViewMocking = false
        MockableMavericks.enableMockPrinterBroadcastReceiver = false
        Mavericks.viewModelDelegateFactory = DefaultViewModelDelegateFactory()
        Mavericks.viewModelConfigFactory = MavericksViewModelConfigFactory(debugMode = debugMode)
    }

    private fun setupMocking() {
        val mocksEnabled = viewModelMockBehavior != null
        // Use a null context since we don't need mock printing during tests
        MockableMavericks.initialize(debugMode = debugMode, mocksEnabled = mocksEnabled, applicationContext = null)

        if (viewModelMockBehavior != null) {
            MockableMavericks.mockConfigFactory.mockBehavior = viewModelMockBehavior
        }
    }
}
