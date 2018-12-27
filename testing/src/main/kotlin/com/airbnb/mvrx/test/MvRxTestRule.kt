package com.airbnb.mvrx.test

import android.support.annotation.NonNull
import com.airbnb.mvrx.MvRxTestOverridesProxy
import io.reactivex.Scheduler
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.CompositeException
import io.reactivex.internal.schedulers.ExecutorScheduler
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

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
         * Sets up all Rx schedulers to use an immediate scheduler. This will cause all MvRx
         * operations including setState reducers to run synchronously so you can test them.
         */
        private val setRxImmediateSchedulers: Boolean = true
) : TestRule {
    private var defaultExceptionHandler: Thread.UncaughtExceptionHandler? = null

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                RxAndroidPlugins.reset()
                RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
                RxAndroidPlugins.setMainThreadSchedulerHandler { Schedulers.trampoline() }
                if (setRxImmediateSchedulers) setRxImmediateSchedulers()
                MvRxTestOverridesProxy.forceMvRxDebug(debugMode.value)
                try {
                    base.evaluate()
                } finally {
                    RxAndroidPlugins.reset()
                    MvRxTestOverridesProxy.forceMvRxDebug(DebugMode.Unset.value)
                    if (setRxImmediateSchedulers) clearRxImmediateScheduleres()
                }
            }
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

    private fun clearRxImmediateScheduleres() {
        Thread.setDefaultUncaughtExceptionHandler(defaultExceptionHandler)
        defaultExceptionHandler = null
        RxJavaPlugins.reset()
    }
}