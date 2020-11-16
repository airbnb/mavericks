package com.airbnb.mvrx.mocking

import android.os.Parcelable
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.mocking.printer.MavericksMockPrinter

interface MockableMavericksView : MavericksView {
    /**
     * Override this to provide the mock states that should be used for testing this view.
     *
     * It is intended that functions such as [mockSingleViewModel] be used to provide the value
     * of this function, instead of instantiating MavericksViewMocks directly
     *
     * You should NOT invoke this function directly. You can access the mocks for a view
     * via [MavericksViewMocks.getFrom] instead.
     *
     * For implementing this function, see:
     * @see mockNoViewModels
     * @see mockSingleViewModel
     * @see combineMocks
     *
     * For helpers to access mocks provided by this function, see:
     * @see MavericksViewMocks.getFrom
     * @see getMockVariants
     * @see mockVariants
     */
    fun provideMocks(): MavericksViewMocks<out MockableMavericksView, out Parcelable> = EmptyMocks

    /**
     * Register this view to listen for broadcast receiver intents for mock state
     * printing. This is safe to call multiple times for the same [MavericksView].
     *
     * Doing this allows the arguments for the view to be printed out. View model states
     * will be printed out if [MockableMavericks.initialize] has been done.
     *
     * This should be called when the view is created, so it is available to have its state
     * printed. This is a no-op if [MockableMavericks.enableMockPrinterBroadcastReceiver] is disabled.
     *
     * For more about mock printing, see [MavericksPrintStateBroadcastReceiver]
     */
    fun registerMockPrinter() {
        MavericksMockPrinter.startReceiver(this)
    }
}
