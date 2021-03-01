package com.airbnb.mvrx.launcher

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.util.Log
import androidx.core.os.postDelayed
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.MavericksViewModelConfig
import com.airbnb.mvrx.launcher.utils.toastLong
import com.airbnb.mvrx.mocking.MockBehavior
import com.airbnb.mvrx.mocking.MockedView
import com.airbnb.mvrx.mocking.MockedViewProvider
import com.airbnb.mvrx.mocking.MavericksMock
import kotlin.reflect.KClass

/**
 * This launches a single mocked fragment.
 *
 * Once initial mocked state is set, the normal mutable state functionality is restored, and more fragments can be launched from in.
 * It should be behave like a totally normal screen, with only the initial setup forced.
 *
 * If the fragment crashes on load, this will attempt to handle the exception and gracefully
 * alert you to the issue and return to the main launcher page.
 *
 * Note: If you want to use a custom activity to host your
 */
class MavericksLauncherMockActivity : MavericksBaseLauncherActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            showNextMockFromActivity(
                activity = this,
                showView = { view ->
                    // Use commit now to catch errors on initialization.
                    setFragment(view as Fragment, commitNow = true)
                }
            )
        }
    }

    companion object {
        private val tag = MavericksLauncherMockActivity::class.java.simpleName
        private const val FINISH_AFTER_LAUNCH = "EXTRA_FINISH_AFTER_LAUNCH"

        var nextMockToShow: MockedViewProvider<*>? = null
            private set

        /**
         * Specify which activity should be used to display mocked views when a view is launched
         * from the Mavericks launcher.
         *
         * @see [intent]
         */
        var activityToShowMock: KClass<out Activity> = MavericksLauncherMockActivity::class

        /**
         * Specify a function to call before finishing each mock when running a test all scenario.
         */
        var onMockLoaded: (activity: Activity, MockedViewProvider<*>, MockedView<*>) -> Unit = { _, _, _ -> }

        /**
         * Creates an intent to show the given mock, by setting [nextMockToShow].
         *
         * By default this uses [MavericksLauncherMockActivity], however, you can change
         * [activityToShowMock] in order to specify a custom activity that should be started
         * instead.
         *
         * Your custom activity can reference [nextMockToShow] to determine details about the
         * mock to show, and can use the helper function [showNextMock] to easily set up the mock.
         *
         * @param finishAfterLaunch If true, the activity will finish after it has launched the mock
         * and verified that it didn't crash. This is for automated testing purposes.
         */
        fun intent(
            context: Context,
            mock: MockedViewProvider<*>,
            finishAfterLaunch: Boolean = false
        ): Intent {
            Log.d(
                tag,
                "Creating mavericks launcher intent for ${activityToShowMock::class.java.simpleName} for view ${mock.viewName}"
            )
            nextMockToShow = mock
            return Intent(context, activityToShowMock.java).apply {
                putExtra(FINISH_AFTER_LAUNCH, finishAfterLaunch)
            }
        }

        /**
         * Shows the mock set by [nextMockToShow], using the given callback to display the view.
         * It is an error to call this if a mock is not set.
         *
         * @param showView This callback must present the given view (eg if it's a Fragment, add it to the activity).
         * The implementation will depend on the type of [MavericksView].
         * It is recommended that the view be added synchronously so that this can catch and handle
         * errors gracefully if there is an exception thrown when it is shown. Otherwise the launcher
         * may get into an infinite loop as it restarts and tries to show the same mock again automatically.
         *
         * @param onFailure Called if an error is caught while the view is being shown. Use this to
         * gracefully recover and exit from where you are showing the mock.
         *
         * @see showNextMockFromActivity
         */
        private fun showNextMock(
            showView: (MavericksView) -> Unit,
            onFailure: (Throwable) -> Unit = { throw it }
        ) {
            val mock = nextMockToShow ?: error("Mock was not set")

            val mockedView: MockedView<*> = mock.createView(mockBehavior(mock.mock))

            val view = mockedView.viewInstance
            view.lifecycle.addObserver(ViewModelEnabler(mockedView, mock))

            @Suppress("Detekt.TooGenericExceptionCaught")
            try {
                showView(view)
            } catch (e: Throwable) {
                Log.e(
                    "Mavericks Launcher",
                    "${view.javaClass.simpleName} crashed while opening.",
                    e
                )
                onFailure(e)
            }
        }

        /**
         * Similar to [showNextMock], but with a few common defaults added for the case
         * where a view is shown from an Activity.
         */
        fun showNextMockFromActivity(
            activity: Activity,
            showView: Activity.(MavericksView) -> Unit
        ) {
            val mock = nextMockToShow ?: error("Mock was not set")

            val mockedView: MockedView<*> = mock.createView(mockBehavior(mock.mock))

            val view = mockedView.viewInstance
            view.lifecycle.addObserver(ViewModelEnabler(mockedView, mock))

            @Suppress("Detekt.TooGenericExceptionCaught")
            try {
                activity.showView(view)

                if (activity.intent.getBooleanExtra(FINISH_AFTER_LAUNCH, false)) {
                    // If we are initializing from arguments then network requests will be
                    // made, and we want to give enough time for them to complete to make
                    // sure the parsing and display it tested as well.
                    val isInitializing =
                        mock.mock.isDefaultInitialization || mock.mock.isForProcessRecreation
                    val finishAfterMs: Long = if (isInitializing) 3000 else 500

                    Handler().postDelayed(finishAfterMs) {
                        onMockLoaded(activity, mock, mockedView)
                        activity.finish()
                    }
                }
            } catch (e: Throwable) {
                Log.e(
                    "Mavericks Launcher",
                    "${view.javaClass.simpleName} crashed while opening.",
                    e
                )
                // We finish the Activity in order to clear this Fragment as the "current"
                // fragment, so on relaunch it doesn't keep trying to
                // open the same Fragment, which would get stuck in a crash loop.
                activity.toastLong("Fragment crashed - see logcat for stacktrace.")
                activity.finish()
            }
        }

        /**
         * Return the mock behavior that should be used to set up the given mock state.
         */
        private fun mockBehavior(mock: MavericksMock<out MavericksView, out Parcelable>): MockBehavior {
            return when {
                // The fragment is created with mocked arguments and then follows the normal initialization and behavior path.
                // However, if there are "existing" view models then those are expected to have mocked state.
                // For process recreation we only mock the initial state provided to the viewmodel constructor,
                // but then it should run it's normal code paths using that initial state
                mock.forInitialization || mock.isForProcessRecreation -> MockBehavior(
                    initialStateMocking = MockBehavior.InitialStateMocking.Partial,
                    stateStoreBehavior = MockBehavior.StateStoreBehavior.Normal,
                    blockExecutions = MavericksViewModelConfig.BlockExecutions.No
                )
                // The Fragment is fully mocked out and we prevent initialization code from overriding the mock.
                // However, our ViewModelEnabler will later toggle executions to be allowed, once initialization is over,
                // so that the fragment is functional from the state we set it up in.
                else -> MockBehavior(
                    initialStateMocking = MockBehavior.InitialStateMocking.Full,
                    stateStoreBehavior = MockBehavior.StateStoreBehavior.Scriptable,
                    blockExecutions = MavericksViewModelConfig.BlockExecutions.Completely
                )
            }
        }
    }
}
