package com.airbnb.mvrx.compose

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
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
    val composeTestRule = createAndroidComposeRule<ArgsTestActivity>()

    @Before
    fun setUp() {
        Mavericks.initialize(composeTestRule.activity)
    }

    @Test
    fun argumentsAreProperlyUsedToInitializeState() {
        composeTestRule.onNodeWithText("Counter value: 5").assertExists()
    }
}

class ArgsTestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Column {
                val viewModel: CounterViewModel = mavericksViewModel(argsFactory = { ArgumentsTest(5) })

                val state by viewModel.collectAsState()
                Text("Counter value: ${state.count}")
                Button(onClick = viewModel::incrementCount) {
                    Text(text = "Increment")
                }
            }
        }
    }
}
