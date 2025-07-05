package com.airbnb.mvrx.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.airbnb.mvrx.Mavericks
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class Navigation3Test {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<Navigation3TestActivity>()

    @Before
    fun setUp() {
        Mavericks.initialize(composeTestRule.activity)
    }

    @Test
    fun `activity_viewModel is not null`() {
        assertNotNull(composeTestRule.activity.viewModel)
    }
}

class Navigation3TestActivity : ComponentActivity() {
    var viewModel: CounterViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            ComposeView(this).apply {
                setContent {
                    NavDisplay(
                        backStack = remember { mutableStateListOf<Any>("Key") },
                        entryDecorators = listOf(
                            rememberSceneSetupNavEntryDecorator(),
                            rememberSavedStateNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator()
                        ),
                        entryProvider = { key ->
                            NavEntry(key) {
                                viewModel = mavericksViewModel<CounterViewModel, CounterState>()
                            }
                        }
                    )
                }
            }
        )
    }
}