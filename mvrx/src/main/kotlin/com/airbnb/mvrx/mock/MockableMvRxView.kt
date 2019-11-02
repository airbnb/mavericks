package com.airbnb.mvrx.mock

import android.os.Parcelable
import com.airbnb.mvrx.MvRxView
import com.airbnb.mvrx.mock.printer.MvRxMockPrinter

interface MockableMvRxView : MvRxView {
    /**
     * Override this to provide the mock states that should be used for testing this view.
     *
     * You should NOT invoke this function directly. You can access the mocks for a view
     * via [MvRxViewMocks.getFrom] instead.
     */
    override fun provideMocks(): MvRxViewMocks<out MvRxView, out Parcelable> = EmptyMocks

    fun registerMockPrinter() {
        MvRxMockPrinter.startReceiver(this)
    }
}