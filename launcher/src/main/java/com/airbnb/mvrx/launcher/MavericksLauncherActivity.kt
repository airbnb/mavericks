package com.airbnb.mvrx.launcher

import android.content.Context
import android.os.Bundle
import com.airbnb.epoxy.EpoxyController
import com.airbnb.mvrx.launcher.utils.buildIntent

/**
 * Entry point for showing all Mavericks Views in the application.
 *
 * You can extend this activity to modify the UI that is displayed, via [buildCustomModels].
 */
open class MavericksLauncherActivity : MavericksBaseLauncherActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            setFragment(MavericksLauncherFragment())
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

        /**
         * Shortcut to create an intent for this activity and start the intent in one step.
         */
        fun show(context: Context) {
            context.startActivity(context.buildIntent<MavericksLauncherActivity>())
        }
    }
}
