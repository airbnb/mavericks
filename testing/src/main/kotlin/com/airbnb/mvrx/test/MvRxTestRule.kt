package com.airbnb.mvrx.test

import com.airbnb.mvrx.CoroutinesStateStore
import com.airbnb.mvrx.MvRxTestOverrides
import com.airbnb.mvrx.MvRxTestOverridesProxy
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.exceptions.CompositeException
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.ExternalResource
import kotlin.coroutines.CoroutineContext

enum class DebugMode(internal val value: Boolean?) {
    Debug(true),
    NotDebug(false),
    Unset(null)
}

class MvRxTestRule(
        /**
         * Forces MvRx to be in debug mode or not.
         */
        private val debugMode: DebugMode = DebugMode.NotDebug,
        /**
         * This will cause all MvRx operations including setState reducers to run synchronously so you can test them.
         */
        private val immediateReducers: Boolean = true,
        private val setForceDisableLifecycleAwareObserver: Boolean = true
) : ExternalResource() {

    @ExperimentalCoroutinesApi
    override fun before() {
        Dispatchers.setMain(TestCoroutineDispatcher())

        MvRxTestOverridesProxy.forceMvRxDebug(debugMode.value)
        MvRxTestOverridesProxy.forceDisableLifecycleAwareObserver(setForceDisableLifecycleAwareObserver)
        if (immediateReducers) {
            MvRxTestOverridesProxy.forceSynchronousStateStores(true)
        }
    }

    @ExperimentalCoroutinesApi
    override fun after() {
        Dispatchers.resetMain()

        MvRxTestOverridesProxy.forceMvRxDebug(DebugMode.Unset.value)
        if (immediateReducers) {
            MvRxTestOverridesProxy.forceSynchronousStateStores(false)
        }
    }
}
