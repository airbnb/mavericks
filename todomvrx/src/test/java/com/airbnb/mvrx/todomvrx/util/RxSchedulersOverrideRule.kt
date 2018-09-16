package com.airbnb.mvrx.todomvrx.util

import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class RxSchedulersOverrideRule : TestRule {

    private val trampolineScheduler = Schedulers.trampoline()

    override fun apply(base: Statement, description: Description) = object : Statement() {
        override fun evaluate() {
            RxAndroidPlugins.reset()
            RxAndroidPlugins.setInitMainThreadSchedulerHandler { trampolineScheduler }

            RxJavaPlugins.reset()
            RxJavaPlugins.setIoSchedulerHandler { trampolineScheduler }
            RxJavaPlugins.setNewThreadSchedulerHandler { trampolineScheduler }
            RxJavaPlugins.setComputationSchedulerHandler { trampolineScheduler }
            RxJavaPlugins.setSingleSchedulerHandler { trampolineScheduler }

            base.evaluate()

            RxAndroidPlugins.reset()
            RxJavaPlugins.reset()
        }
    }
}