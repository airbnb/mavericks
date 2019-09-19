package com.airbnb.mvrx.launcher

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.BaseMvRxActivity
import com.airbnb.mvrx.mock.MockBehavior
import com.airbnb.mvrx.mock.MockedView
import com.airbnb.mvrx.mock.MockedViewProvider

/**
 * This launches a single mocked fragment.
 * Once initial mocked state is set, the normal mutable state functionality is restored, and more fragments can be launched from in.
 * It should be behave like a totally normal screen, with only the initial setup forced.
 */
class ActivityToShowMock : BaseMvRxActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {

            val mock = nextMockToShow ?: error("Mock was not set")
            val mockData = mock.mockData

            val mockBehavior = when {
                // The fragment is created with mocked arguments and then follows the normal initialization and behavior path.
                // However, if there are "existing" view models then those are expected to have mocked state.
                // For process recreation we only mock the initial state provided to the viewmodel constructor,
                // but then it should run it's normal code paths using that initial state
                mockData.forInitialization || mockData.isForProcessRecreation -> MockBehavior(
                    initialState = MockBehavior.InitialState.Partial,
                    stateStoreBehavior = MockBehavior.StateStoreBehavior.Normal,
                    blockExecutions = MockBehavior.BlockExecutions.No
                )
                // The Fragment is fully mocked out and we prevent initialization code from overriding the mock.
                // However, our ViewModelEnabler will later toggle executions to be allowed, once initialization is over,
                // so that the fragment is functional from the state we set it up in.
                else -> MockBehavior(
                    initialState = MockBehavior.InitialState.Full,
                    stateStoreBehavior = MockBehavior.StateStoreBehavior.Scriptable,
                    blockExecutions = MockBehavior.BlockExecutions.Completely
                )
            }

            val mockedView: MockedView<*> = mock.createView(mockBehavior)

            // TODO Support non fragment view types
            val view = mockedView.viewInstance as Fragment
            view.lifecycle.addObserver(ViewModelEnabler(mockedView, mock))

            @Suppress("Detekt.TooGenericExceptionCaught")
            try {
                // Similar to the normal MvRxActivity method to setFragment - but needs to use commit now
                // to catch errors on initialization.
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, view)
                    .setPrimaryNavigationFragment(view)
                    .commitNow()
            } catch (e: Throwable) {
                // We finish the Activity in order to clear this Fragment as the "current" fragment, so on relaunch it doesn't keep trying to
                // open the same Fragment, which would get stuck in a crash loop.
                Log.e(
                    "MvRx Launcher",
                    "${view.javaClass.simpleName} crashed while opening.",
                    e
                )
                Toast.makeText(
                    this,
                    "Fragment crashed - see logcat for stacktrace.",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }

            nextMockToShow = null
        }
    }


    companion object {
        internal var nextMockToShow: MockedViewProvider<*>? = null
    }
}