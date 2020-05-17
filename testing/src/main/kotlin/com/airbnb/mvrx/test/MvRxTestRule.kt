package com.airbnb.mvrx.test

import com.airbnb.mvrx.MvRxTestOverridesProxy
import com.airbnb.mvrx.MvRxViewModelConfigFactory
import com.airbnb.mvrx.mocking.MockBehavior
import com.airbnb.mvrx.mocking.MvRxMocks
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.exceptions.CompositeException
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.rules.ExternalResource

class MvRxTestRule(
        /**
     * Sets up all Rx schedulers to use an immediate scheduler. This will cause all MvRx
     * operations including setState reducers to run synchronously so you can test them.
     */
    private val setRxImmediateSchedulers: Boolean = true,
        /**
     * If true, any subscriptions made to a MvRx view model will NOT be made lifecycle aware.
     * This can make it easier to test subscriptions because you won't have to move the test targets to a
     * STARTED state before they can receive subscriptions.
     */
    private val setForceDisableLifecycleAwareObserver: Boolean = true,
        /**
     * If provided, MvRx mocking will be enabled via [MvRxMocks.install] and this will be set as
     * the mocking behavior. The default behavior simply puts the ViewModel in a configuration
     * where state changes happen synchronously, which is often necessary for tests.
     *
     * You can pass a "Scriptable" state store behavior to prevent state changes while forcing
     * your own state changes.
     *
     * If null is given then mock behavior is disabled via [MvRxMocks.install].
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
    private var defaultExceptionHandler: Thread.UncaughtExceptionHandler? = null

    override fun before() {
        RxAndroidPlugins.reset()
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setMainThreadSchedulerHandler { Schedulers.trampoline() }
        if (setRxImmediateSchedulers) setRxImmediateSchedulers()

        MvRxTestOverridesProxy.forceDisableLifecycleAwareObserver(
            setForceDisableLifecycleAwareObserver
        )

        setupMocking()
    }

    override fun after() {
        RxAndroidPlugins.reset()
        // This is set up after again to clear any changes or listeners that were set on the plugins
        setupMocking()
        if (setRxImmediateSchedulers) clearRxImmediateSchedulers()
    }

    private fun setupMocking() {
        val mocksEnabled = viewModelMockBehavior != null
        // Use a null context since we don't need mock printing during tests
        MvRxMocks.install(debugMode = debugMode, mocksEnabled = mocksEnabled, context = null)

        if (viewModelMockBehavior != null) {
            MvRxMocks.mockConfigFactory.mockBehavior = viewModelMockBehavior
        }
    }

    private fun setRxImmediateSchedulers() {
        RxJavaPlugins.reset()
        RxJavaPlugins.setNewThreadSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setSingleSchedulerHandler { Schedulers.trampoline() }
        // This is necessary to prevent rxjava from swallowing errors
        // https://github.com/ReactiveX/RxJava/issues/5234
        defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            if (e is CompositeException && e.exceptions.size == 1) throw e.exceptions[0]
            throw e
        }
    }

    private fun clearRxImmediateSchedulers() {
        Thread.setDefaultUncaughtExceptionHandler(defaultExceptionHandler)
        defaultExceptionHandler = null
        RxJavaPlugins.reset()
    }
}
