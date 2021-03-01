package com.airbnb.mvrx.launcher

import android.os.Handler
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelConfig
import com.airbnb.mvrx.mocking.MockBehavior
import com.airbnb.mvrx.mocking.MockableMavericksViewModelConfig
import com.airbnb.mvrx.mocking.MockedView
import com.airbnb.mvrx.mocking.MockedViewProvider
import kotlin.reflect.KProperty1

/**
 * Once the initial view state is forced we want the user to be able to take over and use the screen like normal, which requires
 * state to be settable.
 *
 * This swaps the mocked state store to a real one to enable normal usage once the View becomes Resumed for the first time.
 *
 * Finally, it cleans up the mocked state that was stored in the MockStateHolder.
 */
class ViewModelEnabler(
    private val mockedView: MockedView<*>,
    private val mock: MockedViewProvider<*>
) : LifecycleObserver {

    @Suppress("unused")
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        val view = mockedView.viewInstance
        view.lifecycle.removeObserver(this)

        // This is briefly delayed in case any UI initialization causes network events to be
        // triggered, which we don't want overriding the mock state.
        Handler().postDelayed(
            {
                mock.mock
                    .states
                    .map { mockState ->
                        @Suppress("UNCHECKED_CAST")
                        (mockState.viewModelProperty as KProperty1<MavericksView, MavericksViewModel<*>>)
                            .get(view)
                    }
                    .forEach { viewModel ->
                        MockableMavericksViewModelConfig.access(viewModel).pushBehaviorOverride(
                            MockBehavior(
                                blockExecutions = MavericksViewModelConfig.BlockExecutions.No,
                                stateStoreBehavior = MockBehavior.StateStoreBehavior.Normal
                            )
                        )
                    }

                mockedView.cleanupMockState()
            },
            delayTime
        )
    }

    companion object {
        /**
         * Amount of time delay between when the fragment is resumed and when view models
         * have "execution" enabled so that real loads can occur.
         *
         * This is left mutable so users can customize the default. Too short and it may not result
         * in an accurate mock (as requests can override the mock state). Too long and it may
         * block the user's actions if they start using the screen.
         */
        var delayTime: Long = 500
    }
}
