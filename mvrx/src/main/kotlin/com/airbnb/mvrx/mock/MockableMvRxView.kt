package com.airbnb.mvrx.mock

import android.os.Parcelable
import com.airbnb.mvrx.MvRxView
import com.airbnb.mvrx.mock.printer.MvRxMockPrinter

interface MockableMvRxView : MvRxView {
    /**
     * Override this to provide the mock states that should be used for testing this view.
     *
     * It is intended that functions such as [mockSingleViewModel] be used to provide the value
     * of this function, instead of instantiating MvRxViewMocks directly
     *
     * You should NOT invoke this function directly. You can access the mocks for a view
     * via [MvRxViewMocks.getFrom] instead.
     *
     * For implementing this function, see:
     * @see mockNoViewModels
     * @see mockSingleViewModel
     * @see combineMocks
     *
     * For helpers to access mocks provided by this function, see:
     * @see MvRxViewMocks.getFrom
     * @see getMockVariants
     * @see mockVariants
     */
    fun provideMocks(): MvRxViewMocks<out MvRxView, out Parcelable> = EmptyMocks

    /**
     * TODO
     */
    fun registerMockPrinter() {
        MvRxMockPrinter.startReceiver(this)
    }
}