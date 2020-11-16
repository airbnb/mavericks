package com.airbnb.mvrx.sample

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.SparseArrayCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavAction
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.launcher.MavericksLauncherMockActivity.Companion.showNextMockFromActivity

/**
 * This class provides a custom implementation for handling launched mocks, by overriding
 * the default behavior in MvRxLauncherMockActivity.
 *
 * The Application class was overridden to set this behavior change:
 * `MvRxLauncherMockActivity.activityToShowMock = LauncherActivity::class`
 *
 * This is needed so we can support the navigation architecture that is used.
 *
 * Note: The work around to support navigation architecture here works for the sample use case,
 * but is not extensively tested or intended to be a complete solution.
 */
class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            // This is posted so that the nav graph can set its initial fragment first, otherwise
            // it will override the one we set.
            Handler().post {
                showNextMockFromActivity(
                    activity = this,
                    showView = { showFragmentWithNavigation(it) }
                )
            }
        }
    }
}

private fun FragmentActivity.showFragmentWithNavigation(view: MavericksView) {
    // The nav host specified in the activity layout
    val navHostFragment = supportFragmentManager.fragments
        .firstOrNull { it is NavHostFragment }
        ?: error("Could not find nav host fragment")

    // The nav host simply uses its fragment id as the container id
    // when adding fragments internally, so we can use the same approach
    // to force a custom fragment.
    navHostFragment.childFragmentManager
        .beginTransaction()
        .replace(navHostFragment.id, view as Fragment)
        .commitNow()

    // Normally the app would crash if the fragment tried to invoke an action,
    // since it isn't properly registered in the graph. To work around that,
    // we make all actions available via a hack.
    navHostFragment
        .findNavController()
        .makeAllActionsAccessibleToCurrentDestination()
}

/**
 * This finds all of the navigation actions across all fragments in the graph, and adds them
 * all to the current destination so that it is possible to open any navigation link from here.
 */
private fun NavController.makeAllActionsAccessibleToCurrentDestination() {
    val currentDestination = currentDestination ?: return

    val actionsField = NavDestination::class.java.getDeclaredField("mActions")
    actionsField.isAccessible = true

    graph.forEach { dest ->
        @Suppress("UNCHECKED_CAST")
        val actions = (actionsField.get(dest) as SparseArrayCompat<NavAction>?) ?: return@forEach

        for (i in 0 until actions.size()) {
            val actionId = actions.keyAt(i)

            actions.get(actionId)?.let { action ->
                currentDestination.putAction(actionId, action)
            }
        }
    }
}
