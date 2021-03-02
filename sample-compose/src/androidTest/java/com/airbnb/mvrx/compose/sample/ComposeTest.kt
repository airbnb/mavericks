package com.airbnb.mvrx.compose.sample

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.airbnb.mvrx.Mavericks
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ComposeTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComposeSampleActivity>()

    @Before
    fun setUp() {
        Mavericks.initialize(composeTestRule.activity)
    }

    @Test
    fun testViewModel() {
        composeTestRule.onAllNodesWithText("Navigation Scoped Count: 0\nActivity Scoped Count: 0")[0].assertExists()
        composeTestRule.onAllNodesWithText("Navigation Scoped Count: 0\nActivity Scoped Count: 0")[1].assertExists()

        composeTestRule.onAllNodesWithText("Increment Navigation Scoped Count")[0].performClick()

        composeTestRule.onNodeWithText("Navigation Scoped Count: 1\nActivity Scoped Count: 0").assertExists()
        composeTestRule.onNodeWithText("Navigation Scoped Count: 0\nActivity Scoped Count: 0").assertExists()

        composeTestRule.onAllNodesWithText("Increment Activity Scoped Count")[0].performClick()
        composeTestRule.onAllNodesWithText("Increment Activity Scoped Count")[1].performClick()

        composeTestRule.onNodeWithText("Navigation Scoped Count: 1\nActivity Scoped Count: 2").assertExists()
        composeTestRule.onNodeWithText("Navigation Scoped Count: 0\nActivity Scoped Count: 2").assertExists()

        composeTestRule.onAllNodesWithText("Increment Navigation Scoped Count")[1].performClick()
        composeTestRule.onAllNodesWithText("Increment Navigation Scoped Count")[1].performClick()
        composeTestRule.onAllNodesWithText("Increment Navigation Scoped Count")[1].performClick()

        composeTestRule.onNodeWithText("Navigation Scoped Count: 1\nActivity Scoped Count: 2").assertExists()
        composeTestRule.onNodeWithText("Navigation Scoped Count: 3\nActivity Scoped Count: 2").assertExists()
    }
}