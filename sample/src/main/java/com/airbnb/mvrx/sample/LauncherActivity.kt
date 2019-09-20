package com.airbnb.mvrx.sample

import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.collection.SparseArrayCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavAction
import androidx.navigation.NavDestination
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import com.airbnb.mvrx.BaseMvRxActivity
import com.airbnb.mvrx.launcher.MvRxLauncherMockActivity.Companion.showNextMock
import com.airbnb.mvrx.sample.features.dadjoke.DadJokeDetailFragment


/**
 * This class provides a custom implementation for handling launched mocks, by overriding
 * the default behavior in MvRxLauncherMockActivity.
 *
 * This is needed so we can support the navigation architecture that is used.
 *
 * Note: This works to show the fragment, but is not extensively tested or intended to be a complete
 * solution for working with the navigation component. Notably lacking is support for nested
 * graphs - if a fragment is started that is not in the top level navigation graph then it will crash
 * if any navigation is attempted from it as the proper graph for it is not set.
 */
class LauncherActivity : BaseMvRxActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            // This is posted so that the nav graph can set its initial fragment first, otherwise
            // it will override the one we set.
            Handler().post {
                showNextMock(
                    showView = { view ->
                        // The nav host specified in the activity layout
                        val navHostFragment =
                            supportFragmentManager.findFragmentById(R.id.my_nav_host_fragment)
                                ?: error("Could not find nav host fragment")

                        // The nav host simply uses its fragment id as the container id
                        // when adding fragments internally, so we can use the same approach
                        // to force a custom fragment.
                        navHostFragment.childFragmentManager
                            .beginTransaction()
                            .replace(navHostFragment.id, view as Fragment)
                            .commitNow()

                        val navController = navHostFragment.findNavController()

                        val navigator =
                            navController.navigatorProvider.getNavigator<FragmentNavigator>(
                                FragmentNavigator::class.java
                            )

                        // This takes destinations that are not normally available at the top level
                        // graph, and forcibly adds them so that our mock fragment is able to
                        // access them.
                        // TODO  A general way to automatically include all nested destinations
                        // in the graph.
//                        destinationOverrides.map { (destId, kClass) ->
//                            navigator.createDestination().apply {
//                                id = destId
//                                className = kClass.qualifiedName ?: error("No name for $kClass")
//                            }
//                        }.let {
//                            navController.graph.addDestinations(it)
//                        }

                        val actionsField =
                            NavDestination::class.java.getDeclaredField("mActions").apply {
                                isAccessible = true
                            }

                        navController.graph.forEach { dest ->
                            @Suppress("UNCHECKED_CAST") val actions =
                                (actionsField.get(dest) as SparseArrayCompat<NavAction>?) ?: return@forEach

                            for (i in 0 until actions.size()) {
                                val key = actions.keyAt(i)
                                actions.get(key)?.let { action ->
                                    navController.currentDestination?.putAction(key, action)
                                }
                            }
                        }
                    },
                    onFailure = {
                        Toast.makeText(
                            this,
                            "Fragment crashed - see logcat for stacktrace.",
                            Toast.LENGTH_LONG
                        ).show()

                        // We finish the Activity in order to clear this Fragment as the "current"
                        // fragment, so on relaunch it doesn't keep trying to
                        // open the same Fragment, which would get stuck in a crash loop.
                        finish()
                    }
                )
            }
        }
    }
}

private val destinationOverrides = listOf(
    R.id.action_dadJokeIndex_to_dadJokeDetailFragment to DadJokeDetailFragment::class
)