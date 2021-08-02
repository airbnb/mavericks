package com.airbnb.mvrx

import android.os.Bundle
import androidx.fragment.app.Fragment
import org.junit.Test
import org.robolectric.Robolectric

class StateRestorationTest : BaseTest() {
    @Test
    fun testStateRestorationWithBaseTypeDeclarationSucceeds() {
        // Attach an instance of Fragment1, causing the ChildViewModel to be initialized
        val (activityController, fragment1) = createFragment<Fragment1, TestActivity>()
        // Remove Fragment1 from the Activity backstack so we don't try to restore it
        activityController.get().supportFragmentManager.beginTransaction().remove(fragment1).commitNow()
        // Save the Mavericks state of the Activity into a Bundle
        val bundle = Bundle()
        activityController.saveInstanceState(bundle)

        // Restore a new Activity with the previously saved state and attach an instance of Fragment2
        // This mimics the app process being restored with Fragment2 on top of the backstack, which we don't have the ability to directly test
        val newController = Robolectric.buildActivity(TestActivity::class.java)
        newController.setup(bundle)
        val activity = newController.get()
        Fragment2().apply {
            activity.supportFragmentManager
                .beginTransaction()
                .add(this, "TAG")
                .commitNow()
        }
    }

    abstract class ParentViewModel<S : ParentState>(initialState: S) : MavericksViewModel<S>(initialState)

    abstract class ParentState : MavericksState

    class ChildViewModel(initialState: ChildState) : ParentViewModel<ChildState>(initialState)

    data class ChildState(val string: String = "value") : ParentState()

    class Fragment1 : Fragment(), MavericksView {
        private val activityVm: ChildViewModel by activityViewModel { viewModelKey }

        override fun invalidate() {}
    }

    /**
     * This Fragment is intended to be attached after [Fragment1], and declares a [ParentViewModel] instead of [ChildViewModel]. This simulates
     * a setup where [Fragment2] does not have access to the [ChildViewModel] class at compile time.
     */
    class Fragment2 : Fragment(), MavericksView {
        /**
         * We need to use a shared key for retrieving the ViewModel, because Mavericks will otherwise use the declaration site class, which is
         * abstract in this case - we want to retrieve the [ChildViewModel] initialized by [Fragment1]
         */
        private val existingVm: ParentViewModel<ParentState> by existingViewModel { viewModelKey }

        override fun invalidate() {}
    }

    companion object {
        const val viewModelKey = "key for retrieving ChildViewModel"
    }
}
