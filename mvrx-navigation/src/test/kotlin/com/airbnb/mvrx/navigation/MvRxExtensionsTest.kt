package com.airbnb.mvrx.navigation

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import com.airbnb.mvrx.MvRx
import com.airbnb.mvrx.navigation.test.R
import com.airbnb.mvrx.test.MvRxTestRule
import com.airbnb.mvrx.withState
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28]) // SDK 29 required Java 9+
class MvRxExtensionsTest {

    @get:Rule
    val mvrxRule = MvRxTestRule()
    private val factory = DefaultNavigationViewModelDelegateFactory()

    @Before
    fun setup() {
        MvRx.viewModelDelegateFactory = factory
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
}
