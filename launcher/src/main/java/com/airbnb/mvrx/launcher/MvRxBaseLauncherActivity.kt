package com.airbnb.mvrx.launcher

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.BaseMvRxActivity

open class MvRxBaseLauncherActivity : BaseMvRxActivity() {

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