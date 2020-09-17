package com.airbnb.mvrx.launcher

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.launcher.utils.buildIntent
import com.airbnb.mvrx.launcher.utils.buildText
import com.airbnb.mvrx.launcher.utils.dismissSoftKeyboard
import com.airbnb.mvrx.launcher.utils.toastLong
import com.airbnb.mvrx.launcher.utils.toastShort
import com.airbnb.mvrx.launcher.views.loadingRow
import com.airbnb.mvrx.launcher.views.marquee
import com.airbnb.mvrx.launcher.views.textRow
import com.airbnb.mvrx.mocking.MockedViewProvider
import com.airbnb.mvrx.withState

/**
 * Displays all Mavericks screens and mocks that are detected in the app.
 *
 * This is split into two "pages". The first shows the list of view names. Clicking a view
 * updates the screen to show the list of arguments and mocks for that view. Clicking back returns to the list of views.
 *
 * Clicking a mock loads the view in that state in a new activity.
 *
 * [MavericksLauncherTestMocksActivity] is used by default to open each mock. See the documentation on
 * that class for how to customize it, or use a different activity to launch the mock with.
 */
class MavericksLauncherFragment : MavericksLauncherBaseFragment() {

    private val viewModel by fragmentViewModel(MavericksLauncherViewModel::class)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        // This is the main callback for showing the selected mock once it is set.
        viewModel.onEach(
            MavericksLauncherState::selectedMock,
            deliveryMode = uniqueOnly()
        ) { mock ->
            if (mock != null) {
                showSelectedMock(mock)
            }
        }

        viewModel.onEach(MavericksLauncherState::queryResult) { result ->
            // If we're already finishing because we started a different deeplink result, don't
            // start one again. This can happen because the objects don't have strict equals
            // implementations.
            if (activity?.isFinishing != false) return@onEach

            log("Deeplink result: $result")
            when (result) {
                null -> return@onEach
                is QueryResult.NoMatch -> toastLong("No views found matching query '${result.queryText}'")
                is QueryResult.SingleView -> showSelectedMock(result.mock)
                is QueryResult.TestViews -> testMocks(result.mocks)
            }

            // Once we start a new activity to show the matching mocks
            // we finish this one, because multiple deeplink activities don't stack well on each other.
            // The intent is processed when the viewmodel is first created, and subsequent deeplinks are delivered to the same
            // activity, and they don't do anything since the viewmodel already exists.
            // This also prevents a big backstack of activities from being created.
            activity?.finish()
        }
    }

    private fun showSelectedMock(mock: MockedViewProvider<*>) {
        // The activity is started for a result so we can know when
        // it returns (so we can clear the selected mock property)
        startActivityForResult(
            MavericksLauncherMockActivity.intent(requireContext(), mock),
            SHOW_VIEW_REQUEST_CODE
        )
    }

    private val MavericksLauncherState.mocksLoadedSoFar: List<MockedViewProvider<*>>?
        get() = allMocks() ?: cachedMocks()

    private val MavericksLauncherState.mocksForSelectedView: List<MockedViewProvider<*>>?
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

        toolbar.inflateMenu(R.menu.mavericks_launcher_fragment)
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                // This option automatically launches all mocks shown on the screen.
                // They are shown in order just long enough to verify they render and don't crash.
                R.id.menu_mavericks_launcher_auto_run -> {
                    withState(viewModel) { state ->
                        val mocks = state.mocksForSelectedView ?: state.mocksLoadedSoFar ?: return@withState
                        testMocks(mocks)
                    }
                    true
                }
                else -> false
            }
        }
    }

    override fun epoxyController() = simpleController(viewModel) { state ->
        val context = context ?: return@simpleController
        val loadedMocks = state.mocksLoadedSoFar

        val deeplinkQuery = state.viewNamePatternToTest ?: state.viewNameToOpen
        if (deeplinkQuery != null) {
            marquee {
                id("header")
                title("Loading \"$deeplinkQuery\"...")
            }

            loadingRow { id("initial loader") }

            // Waiting for mocks to load, when they do the matching views will be opened and this will be finished.
            return@simpleController
        }

        val viewNameToMocks = loadedMocks
            ?.groupBy { it.viewName }
            ?.toSortedMap(viewUiOrderComparator(state))

        val mocksToShow = state.mocksForSelectedView

        marquee {
            id("header")

            // Get simple name portion of FQN, then remove suffixes that are unnecessarily verbose.
            // This simplifies the UI and makes it prettier.
            val selectedViewName = state.selectedView
                ?.split(".")
                ?.lastOrNull()
                ?.replace("mvrx", "", ignoreCase = true)
                ?.replace("mavericks", "", ignoreCase = true)
                ?.replace("fragment", "", ignoreCase = true)

            val activityName = context::class.java.simpleName
                .replace("activity", "", ignoreCase = true)
                .replace("flavor", "", ignoreCase = true)

            // Format the class name nicely
            title((selectedViewName ?: activityName).splitCamelCase())

            if (mocksToShow != null) {
                subtitle("${mocksToShow.size} mocks")
            } else if (viewNameToMocks != null) {
                subtitle("${viewNameToMocks.size} screens")
            }
        }

        if (state.selectedView == null) {
            // TODO more generic approach
            (activity as MavericksLauncherActivity).addCustomModels(this)
        }

        if (loadedMocks == null || (state.selectedView != null && mocksToShow == null)) {
            loadingRow { id("initial loader") }
            return@simpleController
        }

        mocksToShow
            ?.sortedBy { mockedViewProvider ->
                val recentIndex =
                    state.recentUsage.mockIdentifiers.indexOf(LauncherMockIdentifier(mockedViewProvider))
                if (recentIndex == -1) {
                    Integer.MAX_VALUE
                } else {
                    recentIndex
                }
            }
            ?.forEach { mockedViewProvider ->
                textRow {
                    id("view", mockedViewProvider.viewName, mockedViewProvider.mock.name)
                    title(mockedViewProvider.mock.name)

                    subtitle(buildText(context) {
                        if (LauncherMockIdentifier(mockedViewProvider) in state.recentUsage.mockIdentifiers) {
                            appendWithColor("Recent ", R.color.mavericks_colorPrimary)
                        }

                        if (mockedViewProvider.mock.forInitialization) append("[With Arguments]")
                    })

                    onClickListener { _ ->
                        viewModel.setSelectedMock(mockedViewProvider)
                    }
                }
            } ?: run {
            viewNameToMocks?.forEach { (viewName, mocks) ->
                textRow {
                    id("view entry", viewName)
                    title(viewName.split(".").last())

                    subtitle(buildText(context) {
                        if (viewName in state.recentUsage.viewNames) {
                            appendWithColor("Recent", R.color.mavericks_colorPrimary)
                            append(" · ")
                        }
                        append("${mocks.size} mocks")
                    })

                    onClickListener { _ ->
                        viewModel.setSelectedView(viewName)
                        // If there are any custom rows on the screen that have edit texts the keyboard may be up,
                        // and it won't make sense to keep showing it.
                        view?.dismissSoftKeyboard()
                    }
                }
            }
        }

        if (state.allMocks() == null) {
            loadingRow { id("loading additional mocks") }
        }
    }

    private fun testMocks(mocks: List<MockedViewProvider<*>>) {
        toastShort("Testing ${mocks.size} mocks...")
        MavericksLauncherTestMocksActivity.mocksToShow.clear()
        MavericksLauncherTestMocksActivity.mocksToShow.addAll(mocks)
        startActivity(requireContext().buildIntent<MavericksLauncherTestMocksActivity>())
    }

    override fun onBackPressed() = withState(viewModel) { state ->
        return@withState if (state.selectedView != null) {
            viewModel.setSelectedView(null)
            true
        } else {
            false
        }
    }

    /**
     * When a mock screen is closed we clear it's entry as the current selection.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            SHOW_VIEW_REQUEST_CODE -> viewModel.setSelectedMock(null)
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {
        private const val SHOW_VIEW_REQUEST_CODE = 1
    }
}

private val camelCaseBoundary = "[^A-Z][A-Z]".toRegex()

/** Takes a string in camel case and splits the words so they are instead separated by spaces. */
internal fun String.splitCamelCase(): CharSequence {
    // Finds word boundaries by looking for a lower case letter followed by an uppercase letter
    return replace(camelCaseBoundary) { match ->
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
internal val String.simpleName: String get() = substringAfterLast(".")

internal fun viewUiOrderComparator(state: MavericksLauncherState): Comparator<String> {
    return compareBy { viewName ->
        // Show recently used views first, otherwise compare alphabetically by view name
        val recentViewIndex = state.recentUsage.viewNames.indexOf(viewName)
        if (recentViewIndex == -1) {
            viewName.simpleName
        } else {
            recentViewIndex.toString()
        }
    }
}
