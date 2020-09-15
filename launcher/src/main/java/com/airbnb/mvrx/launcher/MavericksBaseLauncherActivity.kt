package com.airbnb.mvrx.launcher

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

/**
 * Intended to be overridden by other activities in the launcher module.
 * Provides an easy way to set a single fragment.
 */
open class MavericksBaseLauncherActivity : FragmentActivity(R.layout.mavericks_launcher_base_activity) {

    protected fun setFragment(fragment: Fragment, commitNow: Boolean = false) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .setPrimaryNavigationFragment(fragment)
            .apply {
                if (commitNow) commitNow() else commit()
            }
    }
}
