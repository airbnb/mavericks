package com.airbnb.mvrx

import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragment
import org.junit.Test

abstract class ParentViewModel<S : ParentState>(initialState: S) : MavericksViewModel<S>(initialState)

abstract class ParentState : MavericksState

class ChildViewModel(initialState: ChildState) : ParentViewModel<ChildState>(initialState)

data class ChildState(val string: String = "value") : ParentState()

open class FragmentWithAbstractViewModelDeclaration: Fragment(), MavericksView {
    protected val parentViewModel: ParentViewModel<out ParentState> by fragmentViewModel(viewModelClass = ChildViewModel::class, stateClass = ChildState::class)

    override fun invalidate() {}
}

class AbstractViewModelDeclarationTest : BaseTest() {
    /**
     * Tests that [FragmentWithAbstractViewModelDeclaration] can create an instance of [ChildViewModel]
     * using [KClass] arguments to the delegates in MavericksExtensions.kt, while declaring it as
     * an abstract parent class.
     */
    @Test
    fun testFragmentCreationDoesNotError() {
        launchFragment<FragmentWithAbstractViewModelDeclaration>()
    }
}