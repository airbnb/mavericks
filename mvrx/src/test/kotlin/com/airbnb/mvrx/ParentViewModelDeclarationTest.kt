package com.airbnb.mvrx

import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragment
import org.junit.Test

abstract class ParentViewModel<S : ParentState>(initialState: S) : MavericksViewModel<S>(initialState)

abstract class ParentState : MavericksState

class ChildViewModel(initialState: ChildState) : ParentViewModel<ChildState>(initialState)

class ChildState : ParentState()

class FragmentWithParentViewModelDeclaration: Fragment(), MavericksView {
    val parentViewModel: ParentViewModel<out ParentState> by fragmentViewModel(viewModelClass = ChildViewModel::class, stateClass = ChildState::class)

    override fun invalidate() {}
}

class ParentViewModelDeclarationTest : BaseTest() {
    @Test
    fun testFragmentCreationDoesNotError() {
        launchFragment<FragmentWithParentViewModelDeclaration>()
    }
}