package com.airbnb.mvrx.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import com.airbnb.mvrx.Mavericks
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LifecycleOwnerScopeTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<LifecycleOwnerScopeTestActivity>()

    @Before
    fun setUp() {
        Mavericks.initialize(composeTestRule.activity)
    }

    @Test
    fun `activity_viewModel and activity_activityScopedViewModel are the same`() {
        assertNotNull(composeTestRule.activity.viewModel)
        assertNotNull(composeTestRule.activity.activityScopedViewModel)
        assert(composeTestRule.activity.viewModel === composeTestRule.activity.activityScopedViewModel)
    }

    @Test
    fun `fragment_viewModel and fragment_activityScopedViewModel are different`() {
        assertNotNull(composeTestRule.activity.fragment.viewModel)
        assertNotNull(composeTestRule.activity.fragment.activityScopedViewModel)
        assert(composeTestRule.activity.fragment.viewModel !== composeTestRule.activity.fragment.activityScopedViewModel)
    }

    @Test
    fun `activity_viewModel and fragment_activityScopedViewModel are the same`() {
        assertNotNull(composeTestRule.activity.viewModel)
        assertNotNull(composeTestRule.activity.fragment.activityScopedViewModel)
        assert(composeTestRule.activity.viewModel === composeTestRule.activity.fragment.activityScopedViewModel)
    }

    @Test
    fun `fragment_viewModel and activity_activityScopedViewModel are different`() {
        assertNotNull(composeTestRule.activity.fragment.viewModel)
        assertNotNull(composeTestRule.activity.activityScopedViewModel)
        assert(composeTestRule.activity.fragment.viewModel !== composeTestRule.activity.activityScopedViewModel)
    }
}

class LifecycleOwnerScopeTestActivity : AppCompatActivity() {
    lateinit var fragment: LifecycleOwnerScopeTestFragment
    lateinit var viewModel: CounterViewModel
    lateinit var activityScopedViewModel: CounterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fragmentId = 123
        val fragmentContainerView = FragmentContainerView(this).apply {
            id = fragmentId
        }
        val composeView = ComposeView(this).apply {
            setContent {
                this@LifecycleOwnerScopeTestActivity.viewModel = mavericksViewModel<CounterViewModel, CounterState>()
                this@LifecycleOwnerScopeTestActivity.activityScopedViewModel = mavericksActivityViewModel<CounterViewModel, CounterState>()
            }
        }

        setContentView(
            LinearLayout(this).apply {
                addView(fragmentContainerView)
                addView(composeView)
            }
        )

        fragment = LifecycleOwnerScopeTestFragment()

        supportFragmentManager.beginTransaction()
            .add(
                fragmentId,
                fragment
            )
            .commit()
    }
}

class LifecycleOwnerScopeTestFragment : Fragment() {
    lateinit var viewModel: CounterViewModel
    lateinit var activityScopedViewModel: CounterViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                this@LifecycleOwnerScopeTestFragment.viewModel = mavericksViewModel<CounterViewModel, CounterState>()
                this@LifecycleOwnerScopeTestFragment.activityScopedViewModel = mavericksActivityViewModel<CounterViewModel, CounterState>()
            }
        }
    }
}
