package com.airbnb.mvrx.navigation

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import com.airbnb.mvrx.Mavericks
import com.airbnb.mvrx.navigation.test.R
import com.airbnb.mvrx.test.MvRxTestRule
import com.airbnb.mvrx.withState
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@Suppress("DEPRECATION")
@RunWith(RobolectricTestRunner::class)
@LooperMode(LooperMode.Mode.LEGACY)
@Config(sdk = [28]) // SDK 29 required Java 9+
class MavericksExtensionsTest {

    @get:Rule
    val mvrxRule = MvRxTestRule()
    private val factory = DefaultNavigationViewModelDelegateFactory()

    @Before
    fun setup() {
        HostFragment.accessViewModelInOnCreate = false
        Mavericks.viewModelDelegateFactory = factory
    }

    @Test
    fun `can viewModel be created using navigation backStack and consumed by follow fragments`() {

        launchFragmentInContainer(instantiate = { HostFragment() }).onFragment { fragment ->
            val navController = Navigation.findNavController(fragment.requireView())
            navController.navigate(R.id.action_store_to_consumer)

            val viewModel = fragment.viewModel
            requireNotNull(viewModel) {
                "ViewModel was not created by navigation graph viewModels"
            }
            withState(viewModel) { state ->
                assert(state.producer == FirstTestNavigationFragment.TEST_VALUE)
                assert(state.consumer == SecondTestNavigationFragment.TEST_VALUE)
            }
        }
    }

    @Test
    fun `can viewModel be created and restored when activity is re-created`() {

        launchFragmentInContainer(instantiate = { HostFragment() }).also { fragmentScenario ->
            fragmentScenario
                .onFragment { fragment ->
                    val navController = Navigation.findNavController(fragment.requireView())
                    navController.navigate(R.id.action_store_to_consumer)

                    val viewModel = fragment.viewModel
                    requireNotNull(viewModel) {
                        "ViewModel was not created by navigation graph viewModels"
                    }
                }
                .recreate()
                .onFragment { fragment ->

                    val viewModel = fragment.viewModel
                    requireNotNull(viewModel) {
                        "ViewModel was not created by navigation graph viewModels"
                    }
                    withState(viewModel) { state ->
                        assert(state.producer == FirstTestNavigationFragment.TEST_VALUE)
                        assert(state.consumer == SecondTestNavigationFragment.TEST_VALUE)
                    }
                }
        }
    }

    @Test(expected = IllegalNavigationStateException::class)
    fun `accessing the viewModel before onViewCreate after an Activity is recreated will result in an IllegalNavigationStateException`() {
        HostFragment.accessViewModelInOnCreate = true
        launchFragmentInContainer(instantiate = { HostFragment() }).also { fragmentScenario ->
            fragmentScenario
                .onFragment { fragment ->
                    val navController = Navigation.findNavController(fragment.requireView())
                    navController.navigate(R.id.action_store_to_consumer)

                    val viewModel = fragment.viewModel
                    requireNotNull(viewModel) {
                        "ViewModel was not created by navigation graph viewModels"
                    }
                }
                .recreate()
                .onFragment { fragment ->

                    val viewModel = fragment.viewModel
                    requireNotNull(viewModel) {
                        "ViewModel was not created by navigation graph viewModels"
                    }
                    withState(viewModel) { state ->
                        assert(state.producer == FirstTestNavigationFragment.TEST_VALUE)
                        assert(state.consumer == SecondTestNavigationFragment.TEST_VALUE)
                    }
                }
        }
    }
}
