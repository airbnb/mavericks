package com.airbnb.mvrx.launcher

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.launcher.views.marquee
import com.airbnb.mvrx.mock.MockedViewProvider
import com.airbnb.mvrx.withState

/**
 * Displays all MvRx screens and mocks.
 * This is split into two "pages". The first shows the list of view names. Clicking a view
 * updates the screen to show the list of arguments and mocks for that view. Clicking back returns to the list of views.
 *
 * Clicking a mock loads the view in that state.
 */
class MvRxLauncherFragment : LauncherBaseFragment() {

    private val viewModel: LauncherViewModel by fragmentViewModel()

    override fun enableMockPrinterReceiver() {
        // Disabling this because we don't want it spitting it out its mocks because it clutters
        // logcat and the user won't want these states included in their output.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var isFirstValue = true
        viewModel.selectSubscribe(
            LauncherState::selectedMock,
            deliveryMode = uniqueOnly()
        ) { mock ->
            // If this fragment is recreated (eg rotation) then the old value will be redelivered. But we don't
            // want to show the fragment in that case, we only want to show it if the activity is starting for the first time.
            val skipFragmentShowing = isFirstValue && savedInstanceState != null
            isFirstValue = false

            if (mock != null && !skipFragmentShowing) {
                showSelectedMock(mock)
            }
        }

        // For handling deeplink cases, we wait until all mocks are loaded and then look at deeplink params for processing
        viewModel.asyncFirstSuccess(LauncherState::allMocks) { allMocks ->
            withState(viewModel) { state ->
                val (query, mocks) = when {
                    state.viewNameToOpen != null -> {
                        // Look for the first Fragment who's simple name contains the deeplink query
                        // We will just use it's default initial args and state
                        val fragmentMatch = allMocks
                            .firstOrNull {
                                it.viewName.simpleName.contains(
                                    state.viewNameToOpen,
                                    ignoreCase = true
                                ) && it.mockData.isDefaultInitialization
                            }

                        // Or if no fragment name matches we can look for a specific mock by name
                        val mock = fragmentMatch ?: allMocks.firstOrNull {
                            it.mockData.name.contains(
                                state.viewNameToOpen,
                                ignoreCase = true
                            )
                        }
                        val mocks = mock?.let { listOf(it) } ?: emptyList()

                        state.viewNameToOpen to mocks
                    }
                    state.viewNamePatternToTest != null -> {
                        fun String.match() =
                            contains(state.viewNamePatternToTest, ignoreCase = true)

                        val mocks =
                            allMocks.filter { it.viewName.match() || it.mockData.name.match() }
                        state.viewNamePatternToTest to mocks
                    }
                    else -> return@withState
                }

                when {
                    mocks.isEmpty() -> toastLong("No fragments found matching query '$query'")
                    state.viewNameToOpen != null -> showSelectedMock(mocks.single())
                    else -> testMocks(mocks)
                }

                // We finish the activity because multiple deeplink activities don't stack well on each other.
                // The intent is processed when the viewmodel is first created, and subsequent deeplinks are delivered to the same
                // activity, and they don't do anything since the viewmodel already exists.
                // This also prevents a big backstack of activities from being created.
                activity?.finish()
            }
        }
    }

    private fun showSelectedMock(mock: MockedViewProvider<*>) {
        ActivityToShowMock.nextMockToShow = mock
        // The activity is started for a result so we can know when it returns (so we can clear the selected mock property)
        startActivityForResult(
            requireContext().buildIntent<ActivityToShowMock>(),
            SHOW_FRAGMENT_REQUEST_CODE
        )
    }

    private val LauncherState.mocksLoadedSoFar: List<MockedViewProvider<*>>?
        get() = allMocks() ?: cachedMocks()

    private val LauncherState.mocksForSelectedFragment: List<MockedViewProvider<*>>?
        get() {
            val loadedMocks = mocksLoadedSoFar ?: return null
            return if (selectedView != null) loadedMocks.filter { it.viewName == selectedView } else null
        }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    recyclerView.dismissSoftKeyboard()
                }
            }
        })
    }

    override fun epoxyController() = simpleController(viewModel) { state ->
        val context = context ?: return@simpleController
        val loadedMocks = state.mocksLoadedSoFar

        val deeplinkQuery = state.viewNamePatternToTest ?: state.viewNameToOpen
//        if (deeplinkQuery != null) {
//            documentMarquee {
//                id("header")
//                title("Loading \"$deeplinkQuery\"...")
//            }
//
//            loaderRow("initial loader")
//
//            // Waiting for mocks to load, when they do the matching fragments will be opened and this will be finished.
//            return@simpleController
//        }

        val viewNameToMocks = loadedMocks
            ?.groupBy { it.viewName }
            ?.toSortedMap(viewUiOrderComparator(state))

        val mocksToShow = state.mocksForSelectedFragment

        marquee {
            id("header")

            // Get simple name portion of FQN, then remove suffixes that are unnecessarily verbose.
            // This simplifies the UI and makes it prettier.
            val selectedViewName = state.selectedView
                ?.split(".")
                ?.lastOrNull()
                ?.replace("mvrx", "", ignoreCase = true)
                ?.replace("fragment", "", ignoreCase = true)

            val activityName = context::class.java.simpleName
                .replace("activity", "", ignoreCase = true)
                .replace("flavor", "", ignoreCase = true)

            // Format the class name nicely
            title((selectedViewName ?: activityName).splitCamelCase())

            if (mocksToShow != null) {
                subtitle("${mocksToShow.size} mocks")
            } else if (viewNameToMocks != null) {
                subtitle("${viewNameToMocks.size} fragments")
            }
        }

        if (state.selectedView == null) {
            // TODO more generic approach
            (activity as MvRxLauncherActivity)?.addCustomModels(this)
        }

//        if (loadedMocks == null || (state.selectedView != null && mocksToShow == null)) {
//            loaderRow("initial loader")
//            return@simpleController
//        }
//
//        mocksToShow
//            ?.sortedBy {
//                val recentIndex = state.recentUsage.mockIdentifiers.indexOf(MockIdentifier(it))
//                if (recentIndex == -1) {
//                    Integer.MAX_VALUE
//                } else {
//                    recentIndex
//                }
//            }
//            ?.forEach {
//                basicRow {
//                    id("fragment", it.viewName, it.mockData.name)
//                    title(it.mockData.name)
//
//                    subtitleText(buildText(context) {
//                        if (MockIdentifier(it) in state.recentUsage.mockIdentifiers) {
//                            appendWithColor("Recent ", R.color.n2_babu)
//                        }
//
//                        if (it.mockData.forInitialization) append("[With Arguments]")
//                    })
//
//                    onClickListener { _ ->
//                        viewModel.setSelectedMock(it)
//                    }
//                }
//            } ?: run {
//            viewNameToMocks?.forEach { (viewName, mocks) ->
//                basicRow {
//                    id("fragment", viewName)
//                    title(viewName.split(".").last())
//
//                    subtitleText(buildText(context) {
//                        if (viewName in state.recentUsage.viewNames) {
//                            appendWithColor("Recent", R.color.n2_babu)
//                            append(" · ")
//                        }
//                        append("${mocks.size} mocks")
//                    })
//
//                    onClickListener { _ ->
//                        viewModel.setSelectedView(viewName)
//                        // If there are any custom rows on the screen that have edit texts the keyboard may be up,
//                        // and it won't make sense to keep showing it.
//                        activity?.let { dismissSoftKeyboard(it) }
//                    }
//                }
//            }
//        }
//
//        if (state.allMocks() == null) {
//            loaderRow("loading additional mocks")
//        }
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            // This option automatically launches all mocks shown on the screen.
//            // They are shown in order just long enough to verify they render and don't crash.
//            R.id.menu_mvrx_launcher_auto_run -> {
//                withState(viewModel) { state ->
//                    val mocks =
//                        state.mocksForSelectedFragment ?: state.mocksLoadedSoFar ?: return@withState
//                    testMocks(mocks)
//                }
//
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }

    private fun testMocks(mocks: List<MockedViewProvider<*>>) {
        toastShort("Testing ${mocks.size} mocks...")
        ActivityToTestMocks.mocksToShow.addAll(mocks)
        startActivity(requireContext().buildIntent<ActivityToTestMocks>())
    }

//    override fun onBackPressed(): Boolean = withState(viewModel) { state ->
//        return@withState if (state.selectedView != null) {
//            viewModel.setSelectedView(null)
//            true
//        } else {
//            super.onBackPressed()
//        }
//    }
//
//    override fun onHomeActionPressed(): Boolean = withState(viewModel) { state ->
//        return@withState if (state.selectedView != null) {
//            viewModel.setSelectedView(null)
//            true
//        } else {
//            super.onHomeActionPressed()
//        }
//    }

    /**
     * When a mock screen is closed we clear it's entry as the current selection.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            SHOW_FRAGMENT_REQUEST_CODE -> viewModel.setSelectedMock(null)
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {
        private const val SHOW_FRAGMENT_REQUEST_CODE = 1
    }
}

/** Takes a string in camel case and splits the words so they are instead separated by spaces. */
private fun String.splitCamelCase(): CharSequence {
    // Finds word boundaries by looking for a lower case letter followed by an uppercase letter
    return replace("[^A-Z][A-Z]".toRegex()) { match ->
        val twoLetters = match.value
        if (twoLetters == "vR") {
            // If this matches "MvRx" we don't want to split that
            twoLetters
        } else {
            StringBuilder(twoLetters).insert(1, " ").toString()
        }
    }
}

/** Assumes a FQN - returns the simple name. */
private val String.simpleName: String get() = substringAfterLast(".")

internal fun viewUiOrderComparator(state: LauncherState): Comparator<String> {
    return compareBy { viewName ->
        // Show recently used fragments first, otherwise compare alphabetically by fragment name
        val recentFragmentIndex = state.recentUsage.viewNames.indexOf(viewName)
        if (recentFragmentIndex == -1) {
            viewName.simpleName
        } else {
            recentFragmentIndex.toString()
        }
    }
}
