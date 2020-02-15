// This suppression may be due to a bug in Detekt because this is an abstract class.
@file:Suppress("UtilityClassWithPublicConstructor")

package com.airbnb.mvrx

import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Runnable
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.shadows.ShadowLog
import kotlin.coroutines.CoroutineContext

@Suppress("EXPERIMENTAL_API_USAGE")
@RunWith(RobolectricTestRunner::class)
@Ignore("Base Class")
abstract class BaseTest {
    companion object {
        @JvmStatic
        @BeforeClass
        fun classSetUp() {
            ShadowLog.stream = System.out
            CoroutinesStateStore.scopeFactory = {
                CoroutineScope(
                    object : CoroutineDispatcher() {
                        override fun dispatch(context: CoroutineContext, block: Runnable) {
                            block.run()
                        }
                    }
                )
            }
        }
    }

    protected inline fun <reified F : Fragment, reified A : AppCompatActivity> createFragment(
        savedInstanceState: Bundle? = null,
        args: Parcelable? = null,
        containerId: Int? = null
    ): Pair<ActivityController<A>, F> {
        val controller = Robolectric.buildActivity(A::class.java)
        if (savedInstanceState == null) {
            controller.setup()
        } else {
            controller.setup(savedInstanceState)
        }
        val activity = controller.get()
        val fragment = if (savedInstanceState == null) {
            F::class.java.newInstance().apply {
                arguments = Bundle().apply { putParcelable(MvRx.KEY_ARG, args) }
                if (containerId != null) {
                    activity.supportFragmentManager.beginTransaction().add(containerId, this, "TAG").commitNow()
                } else {
                    activity.supportFragmentManager.beginTransaction().add(this, "TAG").commitNow()
                }
            }
        } else {
            activity.supportFragmentManager.findFragmentByTag("TAG") as F
        }
        return controller to fragment
    }

    protected inline fun <reified F : Fragment> ActivityController<out AppCompatActivity>.mvRxFragment(): F {
        return get().supportFragmentManager.findFragmentByTag("TAG") as F
    }
}
