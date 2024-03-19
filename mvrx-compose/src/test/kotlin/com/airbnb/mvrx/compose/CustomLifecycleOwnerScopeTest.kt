package com.airbnb.mvrx.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryOwner
import com.airbnb.mvrx.Mavericks
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CustomLifecycleOwnerScopeTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<CustomLifecycleOwnerScopeTestActivity>()

    @Before
    fun setUp() {
        Mavericks.initialize(composeTestRule.activity)
    }

    @Test
    fun `activity_customScope_viewModel1 and activity_customScope_viewModel2 are different`() {
        assertNotNull(composeTestRule.activity.fragment.viewModel1)
        assertNotNull(composeTestRule.activity.fragment.viewModel2)
        assert(composeTestRule.activity.fragment.viewModel1 !== composeTestRule.activity.fragment.viewModel2)
    }

    @Test
    fun `fragment_customScope_viewModel1 and fragment_customScope_viewModel2 are different`() {
        assertNotNull(composeTestRule.activity.viewModel1)
        assertNotNull(composeTestRule.activity.viewModel2)
        assert(composeTestRule.activity.viewModel1 !== composeTestRule.activity.viewModel2)
    }
}

@Composable
private fun CustomViewModelScope(content: @Composable (LifecycleOwner) -> Unit) {
    val originLifecycleOwner = LocalLifecycleOwner.current
    val customLifecycleRegistry = remember { LifecycleRegistry(originLifecycleOwner) }
    val customScope = remember {
        CustomLifecycleOwner(
            customLifecycleRegistry,
            ViewModelStore(),
            (originLifecycleOwner as SavedStateRegistryOwner).savedStateRegistry
        )
    }

    customLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

    content(customScope)

    DisposableEffect(Unit) {
        onDispose {
            customLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        }
    }
}

class CustomLifecycleOwner(
    override val lifecycle: Lifecycle,
    override val viewModelStore: ViewModelStore,
    override val savedStateRegistry: SavedStateRegistry,
) : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner

class CustomLifecycleOwnerScopeTestActivity : AppCompatActivity() {
    lateinit var fragment: CustomLifecycleOwnerScopeTestFragment
    lateinit var viewModel1: CounterViewModel
    lateinit var viewModel2: CounterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fragmentId = 123
        val fragmentContainerView = FragmentContainerView(this).apply {
            id = fragmentId
        }
        val composeView = ComposeView(this).apply {
            setContent {
                CustomViewModelScope { scope ->
                    this@CustomLifecycleOwnerScopeTestActivity.viewModel1 = mavericksViewModel<CounterViewModel, CounterState>(scope = scope)
                }
                CustomViewModelScope { scope ->
                    this@CustomLifecycleOwnerScopeTestActivity.viewModel2 = mavericksViewModel<CounterViewModel, CounterState>(scope = scope)
                }
            }
        }

        setContentView(
            LinearLayout(this).apply {
                addView(fragmentContainerView)
                addView(composeView)
            }
        )

        fragment = CustomLifecycleOwnerScopeTestFragment()

        supportFragmentManager.beginTransaction()
            .add(
                fragmentId,
                fragment
            )
            .commit()
    }
}

class CustomLifecycleOwnerScopeTestFragment : Fragment() {
    lateinit var viewModel1: CounterViewModel
    lateinit var viewModel2: CounterViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                CustomViewModelScope { scope ->
                    this@CustomLifecycleOwnerScopeTestFragment.viewModel1 = mavericksViewModel<CounterViewModel, CounterState>(scope = scope)
                }
                CustomViewModelScope { scope ->
                    this@CustomLifecycleOwnerScopeTestFragment.viewModel2 = mavericksViewModel<CounterViewModel, CounterState>(scope = scope)
                }
            }
        }
    }
}
