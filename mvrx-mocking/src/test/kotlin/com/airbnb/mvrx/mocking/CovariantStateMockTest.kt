package com.airbnb.mvrx.mocking

import androidx.fragment.app.Fragment
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.fragmentViewModel
import org.junit.Test

class CovariantStateMockTest : BaseTest() {
    abstract class ParentViewModel<S : ParentState>(initialState: S) : MavericksViewModel<S>(initialState)

    abstract class ParentState : MavericksState

    class ChildViewModel(initialState: ChildState) : ParentViewModel<ChildState>(initialState)

    data class ChildState(val string: String = "value") : ParentState()

    open class FragmentWithAbstractViewModelDeclaration : Fragment(), MavericksView {
        protected val parentViewModel: ParentViewModel<out ParentState> by fragmentViewModel(
            viewModelClass = ChildViewModel::class,
            stateClass = ChildState::class
        )

        override fun invalidate() {}
    }

    class FragmentWithParentViewModelDeclarationAndMocks : FragmentWithAbstractViewModelDeclaration(), MockableMavericksView {
        override fun provideMocks() = mockSingleViewModel(
            FragmentWithParentViewModelDeclarationAndMocks::parentViewModel,
            ChildState(),
            null
        ) {}
    }

    @Test
    fun testRetrievingMocks() {
        MavericksViewMocks.getFrom(FragmentWithParentViewModelDeclarationAndMocks())
    }
}