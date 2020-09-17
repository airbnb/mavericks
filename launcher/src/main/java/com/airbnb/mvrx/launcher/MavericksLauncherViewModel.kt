package com.airbnb.mvrx.launcher

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Incomplete
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.ViewModelContext
import com.airbnb.mvrx.launcher.MavericksLauncherActivity.Companion.PARAM_VIEW_PATTERN_TO_TEST
import com.airbnb.mvrx.launcher.MavericksLauncherActivity.Companion.PARAM_VIEW_TO_OPEN
import com.airbnb.mvrx.mocking.MockedViewProvider
import com.airbnb.mvrx.mocking.getMockVariants
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

data class MavericksLauncherState(
    /**
     * Mocks that were loaded in previous app sessions will be restored from cache in this property.
     * It will update incrementally as each cache entry is loaded.
     */
    val cachedMocks: Async<List<MockedViewProvider<*>>> = Loading(),
    /**
     * All mocks that were detected in the app. This can be slow to load, so until it is [Success]
     * the [cachedMocks] can be used.
     */
    val allMocks: Async<List<MockedViewProvider<*>>> = Loading(),
    /** The FQN name of the MavericksView that is currently selected for display. */
    val selectedView: String? = null,
    /**
     * The currently selected mock. This is set when the user clicks into a mock.
     * Additionally, the value is saved and restored when mocks are loaded anew when the viewmodel is initialized.
     */
    val selectedMock: MockedViewProvider<*>? = null,
    /** Details about the most recently used MavericksViews and mocks. The UI can use this to order information for relevance. */
    val recentUsage: LauncherRecentUsage = LauncherRecentUsage(),
    /** A pattern to match views, provided by a deeplink, that should be tested once all mocks load. */
    val viewNamePatternToTest: String? = null,
    /** The name of a specific view or mock, provided by a deeplink, that should be opened directly once all mocks load. */
    val viewNameToOpen: String? = null
) : MavericksState {

    /**
     * If [viewNameToOpen] or [viewNamePatternToTest] are set, this returns the result of which
     * mocks match the deeplink query text. This will only be non null once enough mocks
     * have loaded to provide a comprehensive result.
     */
    val queryResult: QueryResult?
        get() {
            val mocksToCheck = allMocks() ?: cachedMocks() ?: return null

            // We only return "NoMatch" once we are sure all mocks have loaded and we have checked
            // all of them. Until then we can look for matches in mocks as they load incrementally.
            fun fallback(queryText: String) = if (allMocks is Incomplete) {
                null
            } else {
                QueryResult.NoMatch(queryText)
            }

            return when {
                viewNameToOpen != null -> {
                    // Look for the first View who's simple name contains the deeplink query
                    // We will just use it's default initial args and state
                    val viewMatch = mocksToCheck.firstOrNull { mockedViewProvider ->
                        mockedViewProvider.viewName.simpleName.contains(
                            viewNameToOpen,
                            ignoreCase = true
                        ) && mockedViewProvider.mock.isDefaultInitialization
                    }

                    // Or if no view name matches we can look for a specific mock by name
                    val mockMatch = mocksToCheck.firstOrNull { mockedViewProvider ->
                        mockedViewProvider.mock.name.contains(
                            viewNameToOpen,
                            ignoreCase = true
                        )
                    }

                    (viewMatch ?: mockMatch)
                        ?.let { mock ->
                            QueryResult.SingleView(viewNameToOpen, mock)
                        }
                        ?: fallback(viewNameToOpen)
                }
                viewNamePatternToTest != null -> {
                    // Since we need to find all views that match the query we need to make
                    // sure they are all loaded first, and can't just look at incrementally
                    // loaded mocks from the cache.
                    if (allMocks !is Success) return null

                    fun String.match() =
                        contains(viewNamePatternToTest, ignoreCase = true)

                    mocksToCheck
                        .filter { it.viewName.match() || it.mock.name.match() }
                        .takeIf { it.isNotEmpty() }
                        ?.let { mocks ->
                            QueryResult.TestViews(viewNamePatternToTest, mocks)
                        }
                        ?: fallback(viewNamePatternToTest)
                }
                else -> null
            }
        }
}

/**
 * If the launcher was opened with with arguments for a specific screen, this
 * contains the argument query as well as the resulting screens that match it.
 */
sealed class QueryResult {
    /** The text that was specified in the query. */
    abstract val queryText: String

    /** The query was for a single screen, and the given mock was the best match. */
    data class SingleView(override val queryText: String, val mock: MockedViewProvider<*>) :
        QueryResult()

    /** The queryText was to test multiple screens, and all of the mocks matched the naming pattern. */
    data class TestViews(override val queryText: String, val mocks: List<MockedViewProvider<*>>) :
        QueryResult()

    /** No screens matched the given queryText. */
    data class NoMatch(override val queryText: String) : QueryResult()
}

/**
 * Specifies which views and mocks have been recently opened.
 */
data class LauncherRecentUsage(
    /** Ordered list of most recently used MavericksViews, by FQN. */
    val viewNames: List<String> = emptyList(),
    /** Ordered list of most recently used mocks. */
    val mockIdentifiers: List<LauncherMockIdentifier> = emptyList()
) {
    /**
     * Move the given view name to the top of the recents list.
     * Returns a new instance with the list modified.
     */
    fun withViewAtTop(viewName: String?): LauncherRecentUsage {
        if (viewName == null) return this

        return copy(
            viewNames = listOf(viewName) + viewNames.minus(viewName)
        )
    }
}

data class LauncherMockIdentifier(val viewName: String, val mockName: String) {
    constructor(mockedViewProvider: MockedViewProvider<*>) : this(
        mockedViewProvider.viewName,
        mockedViewProvider.mock.name
    )
}

class MavericksLauncherViewModel(
    private val initialState: MavericksLauncherState,
    private val sharedPrefs: SharedPreferences
) : MavericksViewModel<MavericksLauncherState>(initialState) {

    init {
        loadViewsFromCache(initialState)

        GlobalScope.launch {
            val mocks = MavericksGlobalMockLibrary.getMocks()
            log("loaded mocks from state")
            setState {
                // The previously selected view (last time the app ran) may have been deleted or renamed.
                val selectedViewExists = mocks.any { it.viewName == selectedView }

                copy(
                    allMocks = Success(mocks),
                    selectedView = if (selectedViewExists) selectedView else null
                )
            }

            saveViewsToCache(mocks)
        }
    }

    /** Since parsing views from dex files is slow we can remember the last list of view names and load them directly. */
    private fun loadViewsFromCache(initialState: MavericksLauncherState) = GlobalScope.launch {
        val selectedMockData: String? = sharedPrefs.getString(KEY_SELECTED_MOCK, null)
        log("Selected mock from cache: $selectedMockData")

        val (selectedMocksViewName, selectedMockName) = selectedMockData?.split(
            PROPERTY_SEPARATOR
        ) ?: listOf(null, null)

        fun List<MockedViewProvider<*>>.findSelectedMock(): MockedViewProvider<*>? {
            return find { it.viewName == selectedMocksViewName && it.mock.name == selectedMockName }
        }

        // The goal with saving and restoring the selected mock is to launch the last used mock automatically, ASAP, with the expectation
        // that the developer will be needing to use it again (ie ongoing development on that screen.
        // A few seconds can be a big deal here, so to be as instant as possible we sort the views to parse the recent one first,
        // process them all in parallel with coroutines (with the recent mock kicked off first), and as soon as it is ready we update
        // state, without waiting for the rest of the views to finish.
        // This changes the loading time from ~5 seconds to nearly instantaneous (depends how complex the mocks are for the recent view).
        sharedPrefs
            .getList(KEY_VIEWS)
            .sortedWith(viewUiOrderComparator(initialState))
            // Mocks are loaded one at a time, in order recency, so we can prioritize loading
            // the view that is most likely to be needed first.
            .onEach { viewName ->
                try {
                    val mocks = getMockVariants(viewName)

                    // Only set the selected mock if a deeplink wasn't used to open view,
                    // because otherwise they interfere with each other.
                    if (this@MavericksLauncherViewModel.initialState.viewNameToOpen == null && this@MavericksLauncherViewModel.initialState.viewNamePatternToTest == null) {
                        mocks?.findSelectedMock()?.let { selectedMock ->
                            log("Setting selected mock from cache: ${selectedMock.viewName}")
                            setState { copy(selectedMock = selectedMock) }
                        }
                    }

                    log("loaded mocks from cache: $viewName")
                    setState {
                        copy(cachedMocks = Success(cachedMocks().orEmpty() + mocks.orEmpty()))
                    }
                } catch (e: ClassNotFoundException) {
                    // The stored view name might not exist anymore if a different flavor was built or a view was deleted
                    log("Cache class not found: $viewName")
                }
            }
    }

    private fun saveViewsToCache(mockedViewProviders: List<MockedViewProvider<*>>) {
        sharedPrefs.edit {
            // Get fully qualified names and remove duplicates
            putList(KEY_VIEWS, mockedViewProviders.map { it.viewName }.distinct())
        }
    }

    fun setSelectedView(viewName: String?) {
        setState {
            copy(selectedView = viewName, recentUsage = recentUsage.withViewAtTop(viewName))
        }

        sharedPrefs.edit {
            putString(KEY_SELECTED_VIEW, viewName)
            log("Saving selected view to cache:  $viewName")

            if (viewName != null) {
                val recentViews = sharedPrefs.getList(KEY_RECENTLY_USED_VIEWS)
                val newList = listOf(viewName) + recentViews.minus(viewName)
                putList(KEY_RECENTLY_USED_VIEWS, newList.take(NUM_RECENT_ITEMS_TO_KEEP))
            }
        }
    }

    fun setSelectedMock(mock: MockedViewProvider<*>?) {
        setState {
            copy(selectedMock = mock)
        }

        sharedPrefs.edit {
            val savedMockValue =
                if (mock == null) null else mock.viewName + PROPERTY_SEPARATOR + mock.mock.name
            putString(KEY_SELECTED_MOCK, savedMockValue)

            if (savedMockValue != null) {
                val recentMocks = sharedPrefs.getList(KEY_RECENTLY_USED_MOCKS)
                val newList = listOf(savedMockValue) + recentMocks.minus(savedMockValue)
                putList(KEY_RECENTLY_USED_MOCKS, newList.take(NUM_RECENT_ITEMS_TO_KEEP))
            }
        }
    }

    companion object : MavericksViewModelFactory<MavericksLauncherViewModel, MavericksLauncherState> {

        override fun create(
            viewModelContext: ViewModelContext,
            state: MavericksLauncherState
        ): MavericksLauncherViewModel {
            log("Created viewmodel")
            val sharedPrefs = viewModelContext.sharedPrefs()
            return MavericksLauncherViewModel(state, sharedPrefs)
        }

        override fun initialState(viewModelContext: ViewModelContext): MavericksLauncherState? {
            val sharedPrefs = viewModelContext.sharedPrefs()
            val selectedView: String? = sharedPrefs.getString(KEY_SELECTED_VIEW, null)
            val recentViews = sharedPrefs.getList(KEY_RECENTLY_USED_VIEWS)

            val recentMocks = sharedPrefs.getList(KEY_RECENTLY_USED_MOCKS)
                .map { it.split(PROPERTY_SEPARATOR) }
                .map { (viewName, mockName) ->
                    LauncherMockIdentifier(viewName, mockName)
                }

            val params = viewModelContext.activity.intent?.extras
            fun parseParam(name: String) = params?.getString(name)

            return MavericksLauncherState(
                selectedView = selectedView,
                recentUsage = LauncherRecentUsage(
                    viewNames = recentViews,
                    mockIdentifiers = recentMocks
                ),
                viewNamePatternToTest = parseParam(PARAM_VIEW_PATTERN_TO_TEST),
                viewNameToOpen = parseParam(PARAM_VIEW_TO_OPEN)
            )
        }

        private fun ViewModelContext.sharedPrefs(): SharedPreferences {
            return activity.getSharedPreferences("MavericksLauncherCache", Context.MODE_PRIVATE)
        }

        private const val KEY_VIEWS = "key_view_names"
        private const val KEY_SELECTED_VIEW = "key_selected_view"
        private const val KEY_SELECTED_MOCK = "key_selected_mock"
        private const val KEY_RECENTLY_USED_VIEWS = "key_recently_used_views"
        private const val KEY_RECENTLY_USED_MOCKS = "key_recently_used_mocks"
        private const val PROPERTY_SEPARATOR = "|#*#|"
        private const val NUM_RECENT_ITEMS_TO_KEEP = 3
    }
}

// Trying to pick a value that won't accidentally appear in names or descriptions
private const val LIST_SEPARATOR = "**@%$"

private fun SharedPreferences.Editor.putList(
    key: String,
    list: List<String>
): SharedPreferences.Editor? {
    require(list.none { it.contains(LIST_SEPARATOR) }) { "String contained the separator key: $list" }
    log("Putting list at key $key in cache: $list")
    return putString(key, list.joinToString(separator = LIST_SEPARATOR))
}

private fun SharedPreferences.getList(key: String) =
    getString(key, null)?.split(LIST_SEPARATOR) ?: emptyList()

internal fun log(msg: String) {
    Log.d(MavericksLauncherViewModel::class.simpleName, msg)
}
