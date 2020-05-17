package com.airbnb.mvrx.launcher

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

/**
 * Intended to be overridden by other activities in the launcher module.
 * Provides an easy way to set a single fragment.
 */
open class MvRxBaseLauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mvrx_launcher_base_activity)
    }

    protected fun setFragment(fragment: Fragment, commitNow: Boolean = false) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .setPrimaryNavigationFragment(fragment)
            .apply {
                if (commitNow) commitNow() else commit()
            }
    }
}