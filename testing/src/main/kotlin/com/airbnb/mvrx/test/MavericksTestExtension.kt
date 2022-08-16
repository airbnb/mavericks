package com.airbnb.mvrx.test

import com.airbnb.mvrx.mocking.MockBehavior
import com.airbnb.mvrx.mocking.MockableMavericks
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * To use this in your test class, add:
 * ```
 * @RegisterExtension
 * val mavericksExtension = MavericksTestExtension()
 * ```
 */
class MavericksTestExtension(
    /**
     * If true, any subscriptions made to a MavericksViewModel will NOT be made lifecycle aware.
     * This can make it easier to test subscriptions because you won't have to move the test targets to a
     * STARTED state before they can receive subscriptions.
     */
    setForceDisableLifecycleAwareObserver: Boolean = true,
    /**
     * If provided, Mavericks mocking will be enabled via [MockableMavericks.initialize] and this will be set as
     * the mocking behavior. The default behavior simply puts the ViewModel in a configuration
     * where state changes happen synchronously, which is often necessary for tests.
     *
     * You can pass a "Scriptable" state store behavior to prevent state changes while forcing
     * your own state changes.
     *
     * If null is given then mock behavior is disabled via [MockableMavericks.initialize].
     */
    viewModelMockBehavior: MockBehavior? = MockBehavior(
        stateStoreBehavior = MockBehavior.StateStoreBehavior.Synchronous
    ),
    /**
     * Changes whether the [com.airbnb.mvrx.MavericksViewModelConfigFactory] is initialized with debug mode or not.
     * By default debug mode is not used so that viewmodels don't all have to run the debug checks
     * each time they are created for Unit tests. This also prevents the need for Robolectric,
     * since the debug checks use Android APIs.
     */
    debugMode: Boolean = false,
    /**
     * A custom coroutine dispatcher that will be set as Dispatchers.Main for testing purposes.
     */
    @Suppress("EXPERIMENTAL_API_USAGE")
    private val testDispatcher: CoroutineDispatcher = UnconfinedTestDispatcher()
) : BeforeEachCallback, AfterEachCallback {

    private val testLifecycleCallbacks: MavericksTestLifecycleCallbacks = MavericksTestLifecycleCallbacksImpl(
        setForceDisableLifecycleAwareObserver = setForceDisableLifecycleAwareObserver,
        viewModelMockBehavior = viewModelMockBehavior,
        debugMode = debugMode,
        testDispatcher = testDispatcher,
    )

    override fun beforeEach(context: ExtensionContext?) {
        testLifecycleCallbacks.before()
    }

    override fun afterEach(context: ExtensionContext?) {
        testLifecycleCallbacks.after()
    }
}
