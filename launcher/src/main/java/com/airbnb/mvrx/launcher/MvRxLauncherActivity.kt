package com.airbnb.mvrx.launcher

import android.os.Bundle
import com.airbnb.epoxy.EpoxyController

/**
 * Entry point for showing all MvRx Views in the application.
 *
 * You can extend this activity to modify the UI that is displayed, via [buildCustomModels].
 */
open class MvRxLauncherActivity : MvRxBaseLauncherActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            setFragment(MvRxLauncherFragment())
        }
    }

    internal fun addCustomModels(epoxyController: EpoxyController) {
        epoxyController.buildCustomModels()
    }

    /**
     * Override this to insert any custom models you like into the screen.
     * They will be added at the top of the default models, between the header and the list of fragments.
     */
    open fun EpoxyController.buildCustomModels() {
    }

    companion object {
        internal const val PARAM_VIEW_TO_OPEN = "viewNameToOpen"
        internal const val PARAM_VIEW_PATTERN_TO_TEST = "viewNamePatternToTest"
    }
}