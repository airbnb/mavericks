package com.airbnb.mvrx.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import com.airbnb.mvrx.Mavericks
import com.airbnb.mvrx.asMavericksArgs
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FragmentArgsTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<FragmentArgsTestActivity>()

    @Before
    fun setUp() {
        Mavericks.initialize(composeTestRule.activity)
    }

    @Test
    fun argumentsAreProperlyUsedToInitializeState() {
        composeTestRule.onNodeWithText("Counter value: 5").assertExists()
    }
}

class FragmentArgsTestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fragmentContainerView = FragmentContainerView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            id = FRAGMENT_CONTAINER_ID
        }

        setContentView(fragmentContainerView)

        supportFragmentManager.beginTransaction()
            .add(
                FRAGMENT_CONTAINER_ID,
                ArgsTestFragment().apply {
                    arguments = ArgumentsTest(count = 5).asMavericksArgs()
                }
            )
            .commit()
    }
}

class ArgsTestFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        setContent {
            Column {
                val viewModel: CounterViewModel = mavericksViewModel()

                val state by viewModel.collectAsState()
                Text("Counter value: ${state.count}")
                Button(onClick = viewModel::incrementCount) {
                    Text(text = "Increment")
                }
            }
        }
    }
}

const val FRAGMENT_CONTAINER_ID = 123
