// This suppression may be due to a bug in Detekt because this is an abstract class.
@file:Suppress("UtilityClassWithPublicConstructor")
package com.airbnb.mvrx

import android.os.Build
import io.reactivex.Scheduler
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.annotations.NonNull
import io.reactivex.disposables.Disposable
import io.reactivex.internal.schedulers.ExecutorScheduler
import io.reactivex.plugins.RxJavaPlugins
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

@Config(packageName = MvRxTestRunner.PACKAGE_NAME,
        sdk = [Build.VERSION_CODES.LOLLIPOP],
        manifest = MvRxTestRunner.MANIFEST_PATH,
        constants = BuildConfig::class)
@RunWith(MvRxTestRunner::class)
@Ignore
abstract class MvRxBaseTest {
    companion object {
        @JvmStatic
        @BeforeClass
        fun classSetUp() {
            ShadowLog.stream = System.out
            RxAndroidPlugins.reset()
            RxJavaPlugins.reset()
            val immediate = object : Scheduler() {
                // this prevents StackOverflowErrors when scheduling with a delay
                override fun scheduleDirect(@NonNull run: Runnable, delay: Long, @NonNull unit: TimeUnit): Disposable = super.scheduleDirect(run, 0, unit)

                override fun createWorker(): Scheduler.Worker = ExecutorScheduler.ExecutorWorker(Executor { it.run() })
            }
            RxJavaPlugins.setNewThreadSchedulerHandler { immediate }
            RxJavaPlugins.setInitIoSchedulerHandler { immediate }
            RxAndroidPlugins.setInitMainThreadSchedulerHandler { immediate }
        }
    }
}
