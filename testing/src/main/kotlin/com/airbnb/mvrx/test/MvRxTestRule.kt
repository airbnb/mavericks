package com.airbnb.mvrx.test

import com.airbnb.mvrx.DefaultViewModelDelegateFactory
import com.airbnb.mvrx.MvRx
import com.airbnb.mvrx.MvRxTestOverridesProxy
import com.airbnb.mvrx.mocking.MavericksMocks
import com.airbnb.mvrx.mocking.MockBehavior
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.ExternalResource

/**
 * To use this in your test class, add
 * ```
 * @get:Rule
 * val mvrxRule = MvRxTestRule()
 * ```
 */
class MvRxTestRule(
    /**
     * If true, any subscriptions made to a MvRx view model will NOT be made lifecycle aware.
     * This can make it easier to test subscriptions because you won't have to move the test targets to a
     * STARTED state before they can receive subscriptions.
     */
    private val setForceDisableLifecycleAwareObserver: Boolean = true,
    /**
     * If provided, MvRx mocking will be enabled via [MavericksMocks.install] and this will be set as
     * the mocking behavior. The default behavior simply puts the ViewModel in a configuration
     * where state changes happen synchronously, which is often necessary for tests.
     *
     * You can pass a "Scriptable" state store behavior to prevent state changes while forcing
     * your own state changes.
     *
     * If null is given then mock behavior is disabled via [MavericksMocks.install].
     */
    private val viewModelMockBehavior: MockBehavior? = MockBehavior(
        stateStoreBehavior = MockBehavior.StateStoreBehavior.Synchronous
    ),
    /**
     * Changes whether the [MvRxViewModelConfigFactory] is initialized with debug mode or not.
     * By default debug mode is not used so that viewmodels don't all have to run the debug checks
     * each time they are created for Unit tests. This also prevents the need for Robolectric,
     * since the debug checks use Android APIs.
     */
    private val debugMode: Boolean = false
) : ExternalResource() {

    @ExperimentalCoroutinesApi
    override fun before() {
        Dispatchers.setMain(TestCoroutineDispatcher())

        MvRxTestOverridesProxy.forceDisableLifecycleAwareObserver(setForceDisableLifecycleAwareObserver)

        setupMocking()
    }

    private fun setupMocking() {
        val mocksEnabled = viewModelMockBehavior != null
        // Use a null context since we don't need mock printing during tests
        MavericksMocks.install(debugMode = debugMode, mocksEnabled = mocksEnabled, context = null)

        if (viewModelMockBehavior != null) {
            MavericksMocks.mockConfigFactory.mockBehavior = viewModelMockBehavior
        }
    }

    @ExperimentalCoroutinesApi
    override fun after() {
        Dispatchers.resetMain()

        // clear any changes or listeners that were set on the plugins, and reset defaults
        MavericksMocks.enableMavericksViewMocking = false
        MavericksMocks.enableMockPrinterBroadcastReceiver = false
        MvRx.viewModelDelegateFactory = DefaultViewModelDelegateFactory()
        MvRx.viewModelConfigFactory = null
    }


}
