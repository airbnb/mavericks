package com.airbnb.mvrx.compose

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.airbnb.mvrx.Mavericks
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ArgsFactoryTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setUp() {
        Mavericks.initialize(composeTestRule.activity)
    }

    @Test
    fun argumentsAreProperlyUsedToInitializeState() {
        composeTestRule.setContent {
            Column {
                val viewModel: CounterViewModel = mavericksViewModel(argsFactory = { ArgumentsTest(5) })

                val state by viewModel.collectAsState()
                Text("Counter value: ${state.count}")
                Button(onClick = viewModel::incrementCount) {
                    Text(text = "Increment")
                }
            }
        }
        composeTestRule.onNodeWithText("Counter value: 5").assertExists()
    }

    @Test
    fun argumentsAreProperlyUsedToInitializeStateWithMapper() {
        composeTestRule.setContent {
            Column {
                val viewModel: CounterViewModel = mavericksViewModel(argsFactory = { ArgumentsTest(5) })

                val count by viewModel.collectAsState { it.count }
                Text("Counter value: $count")
                Button(onClick = viewModel::incrementCount) {
                    Text(text = "Increment")
                }
            }
        }
        composeTestRule.onNodeWithText("Counter value: 5").assertExists()
    }

    @Test
    fun argumentsAreProperlyUsedToInitializeStateWithMapperAndKey() {
        var collectKey by mutableStateOf(0)
        composeTestRule.setContent {
            Column {
                val viewModel: CounterViewModel = mavericksViewModel(argsFactory = { ArgumentsTest(5) })

                val count by viewModel.collectAsState(collectKey) { if (collectKey == 0) it.count else it.count2 }
                Text("Counter value: $count")
                Button(onClick = viewModel::incrementCount) {
                    Text(text = "Increment")
                }
            }
        }
        composeTestRule.onNodeWithText("Counter value: 5").assertExists()
        collectKey = 1
        composeTestRule.onNodeWithText("Counter value: 123").assertExists()
    }
}
