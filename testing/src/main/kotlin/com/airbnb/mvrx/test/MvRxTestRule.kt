package com.airbnb.mvrx.test

import android.support.annotation.NonNull
import com.airbnb.mvrx.MvRxTestOverridesProxy
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.CompositeException
import io.reactivex.internal.schedulers.ExecutorScheduler
import io.reactivex.plugins.RxJavaPlugins
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
        private val setRxImmediateSchedulers : Boolean = true
) : TestRule {
    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                if (setRxImmediateSchedulers) setRxImmediateSchedulers()
                MvRxTestOverridesProxy.forceMvRxDebug(debugMode.value)
                base.evaluate()
                MvRxTestOverridesProxy.forceMvRxDebug(DebugMode.Unset.value)
                if (setRxImmediateSchedulers) clearRxImmediateScheduleres()
            }
        }
    }

    private fun setRxImmediateSchedulers() {
        RxJavaPlugins.reset()
        val immediate = object : Scheduler() {
            // this prevents StackOverflowErrors when scheduling with a delay
            override fun scheduleDirect(@NonNull run: Runnable, delay: Long, @NonNull unit: TimeUnit): Disposable = super.scheduleDirect(run, 0, unit)

            override fun createWorker(): Scheduler.Worker = ExecutorScheduler.ExecutorWorker(Executor { it.run() })
        }
        RxJavaPlugins.setNewThreadSchedulerHandler { immediate }
        RxJavaPlugins.setComputationSchedulerHandler { immediate }
        RxJavaPlugins.setInitIoSchedulerHandler { immediate }
        RxJavaPlugins.setSingleSchedulerHandler { immediate }
        // This is necessary to prevent rxjava from swallowing errors
        // https://github.com/ReactiveX/RxJava/issues/5234
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            if (e is CompositeException && e.exceptions.size == 1) throw e.exceptions[0]
            throw e
        }
    }

    private fun clearRxImmediateScheduleres() {
        RxJavaPlugins.reset()
    }
}