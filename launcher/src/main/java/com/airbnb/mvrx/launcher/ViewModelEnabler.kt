package com.airbnb.mvrx.launcher

import android.os.Handler
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.MvRxView
import com.airbnb.mvrx.mock.MockBehavior
import com.airbnb.mvrx.mock.MockedView
import com.airbnb.mvrx.mock.MockedViewProvider
import com.airbnb.mvrx.mock.MvRxViewModelConfig
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
        Handler().postDelayed({
            mock.mock
                .states
                .map {
                    @Suppress("UNCHECKED_CAST")
                    (it.viewModelProperty as KProperty1<MvRxView, BaseMvRxViewModel<*>>)
                        .get(view)
                }
                .forEach { viewModel ->
                    MvRxViewModelConfig.access(viewModel).pushBehaviorOverride(
                        MockBehavior(
                            blockExecutions = MockBehavior.BlockExecutions.No,
                            stateStoreBehavior = MockBehavior.StateStoreBehavior.Normal
                        )
                    )
                }

            mockedView.cleanupMockState()
        }, delayTime)
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