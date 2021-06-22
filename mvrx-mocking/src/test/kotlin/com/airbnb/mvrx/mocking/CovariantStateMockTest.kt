package com.airbnb.mvrx.mocking

import android.os.Parcelable
import com.airbnb.mvrx.ChildState
import com.airbnb.mvrx.FragmentWithAbstractViewModelDeclaration
import org.junit.Test

class CovariantStateMockTest {

    class FragmentWithParentViewModelDeclarationAndMocks: FragmentWithAbstractViewModelDeclaration(), MockableMavericksView {
        override fun provideMocks(): MavericksViewMocks<out MockableMavericksView, out Parcelable> {
            return mockSingleViewModel(
                FragmentWithParentViewModelDeclarationAndMocks::parentViewModel,
                ChildState(),
                null
            ) {}
        }
    }

    @Test
    fun testRetrievingMocks() {
        MavericksViewMocks.getFrom(FragmentWithParentViewModelDeclarationAndMocks())
    }
}