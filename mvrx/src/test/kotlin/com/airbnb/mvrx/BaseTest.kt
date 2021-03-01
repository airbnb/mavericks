// This suppression may be due to a bug in Detekt because this is an abstract class.
@file:Suppress("UtilityClassWithPublicConstructor")

package com.airbnb.mvrx

import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowLog

@Suppress("EXPERIMENTAL_API_USAGE", "DEPRECATION")
@LooperMode(LooperMode.Mode.LEGACY)
@RunWith(RobolectricTestRunner::class)
@Ignore("Base Class")
abstract class BaseTest {
    companion object {
        @JvmStatic
        @BeforeClass
        fun classSetUp() {
            ShadowLog.stream = System.out
            MavericksTestOverrides.FORCE_SYNCHRONOUS_STATE_STORES = true
        }

        @JvmStatic
        @AfterClass
        fun classTearDown() {
            MavericksTestOverrides.FORCE_SYNCHRONOUS_STATE_STORES = false
        }
    }

    @Before
    fun setupViewModelConfigFactory() {
        Mavericks.viewModelConfigFactory = MavericksViewModelConfigFactory(true, contextOverride = Dispatchers.Unconfined)
    }

    @After
    fun resetViewModelConfigFactory() {
        Mavericks.viewModelConfigFactory = MavericksViewModelConfigFactory(true)
    }

    protected inline fun <reified F : Fragment, reified A : AppCompatActivity> createFragment(
        savedInstanceState: Bundle? = null,
        args: Parcelable? = null,
        containerId: Int? = null,
        existingController: ActivityController<A>? = null
    ): Pair<ActivityController<A>, F> {
        val controller = existingController ?: Robolectric.buildActivity(A::class.java)

        if (existingController == null) {
            if (savedInstanceState == null) {
                controller.setup()
            } else {
                controller.setup(savedInstanceState)
            }
        }

        val activity = controller.get()
        val fragment = if (savedInstanceState == null) {
            F::class.java.newInstance().apply {
                arguments = Bundle().apply { putParcelable(Mavericks.KEY_ARG, args) }
                activity.supportFragmentManager
                    .beginTransaction()
                    .also {
                        if (containerId != null) {
                            it.add(containerId, this, "TAG")
                        } else {
                            it.add(this, "TAG")
                        }
                    }
                    .commitNow()
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
