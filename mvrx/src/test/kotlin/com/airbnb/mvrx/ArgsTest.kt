package com.airbnb.mvrx

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import kotlinx.parcelize.Parcelize
import org.junit.Assert
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

@Parcelize
data class MvrxArgsTestArgs(val count: Int = 0) : Parcelable

@Parcelize
data class MvrxArgsTestArgs2(val count: Int = 0) : Parcelable

class MvRxArgsFragment : Fragment(), MavericksView {
    val args: MvrxArgsTestArgs by args()

    override fun invalidate() {}
}

class MvRxArgsOrNullFragment : Fragment(), MavericksView {
    val args: MvrxArgsTestArgs? by argsOrNull()

    override fun invalidate() {}
}

class MvRxFragmentTest : BaseTest() {
    @Test
    fun testByArgsWithArgs() {
        val (_, fragment) = createFragment<MvRxArgsFragment, TestMvRxActivity>(args = MvrxArgsTestArgs())
        Assert.assertEquals(0, fragment.args.count)
    }

    @Test
    fun testByArgsSetArgs() {
        val (_, fragment) = createFragment<MvRxArgsFragment, TestMvRxActivity>(args = MvrxArgsTestArgs(2))
        Assert.assertEquals(2, fragment.args.count)
    }

    @Test(expected = ClassCastException::class)
    fun testByArgsSetWrongArgs() {
        val (_, fragment) = createFragment<MvRxArgsFragment, TestMvRxActivity>(args = MvrxArgsTestArgs2(2))
        fragment.args
    }

    @Test(expected = IllegalArgumentException::class)
    fun testByArgsWithNoArgs() {
        val (_, fragment) = createFragment<MvRxArgsFragment, TestMvRxActivity>()
        fragment.args
    }

    @Test
    fun testByArgsOrNullWithArgs() {
        val (_, fragment) = createFragment<MvRxArgsOrNullFragment, TestMvRxActivity>(args = MvrxArgsTestArgs())
        Assert.assertEquals(0, fragment.args?.count)
    }

    @Test
    fun testByArgsOrNullSetArgs() {
        val (_, fragment) = createFragment<MvRxArgsOrNullFragment, TestMvRxActivity>(args = MvrxArgsTestArgs(2))
        Assert.assertEquals(2, fragment.args?.count)
    }

    @Test
    fun testByArgsOrNullWithNoArgs() {
        val (_, fragment) = createFragment<MvRxArgsOrNullFragment, TestMvRxActivity>()
        Assert.assertNull(fragment.args?.count)
    }

    @Test(expected = ClassCastException::class)
    fun testByArgsOrNullSetWrongArgs() {
        val (_, fragment) = createFragment<MvRxArgsOrNullFragment, TestMvRxActivity>(args = MvrxArgsTestArgs2(2))
        fragment.args
    }

    @Test
    fun testNonParcelableArgsErrorContainsClassName() {
        // Create a non-parcelable, non-serializable class for testing
        class NonParcelableArgs(val value: Int)

        val (activityController, fragment) = createFragment<NonParcelableArgsFragment, TestActivity>()

        // Manually create a ViewModel with non-parcelable args using the internal API
        MavericksViewModelProvider.get(
            viewModelClass = TestViewModel::class.java,
            stateClass = TestState::class.java,
            viewModelContext = FragmentViewModelContext(
                activity = activityController.get(),
                args = NonParcelableArgs(42), // Non-parcelable args
                fragment = fragment
            ),
            key = "test_key"
        )

        // Trigger saveInstanceState which should throw the exception
        val exception = assertThrows(IllegalStateException::class.java) {
            val bundle = Bundle()
            activityController.saveInstanceState(bundle)
        }

        // Verify the error message contains the class name
        assertTrue(
            "Error message should contain the class name but was: ${exception.message}",
            exception.message?.contains("NonParcelableArgs") == true
        )
        assertTrue(
            "Error message should contain the expected prefix but was: ${exception.message}",
            exception.message?.contains("Args must be parcelable or serializable") == true
        )
    }

    data class TestState(val value: Int = 0) : MavericksState

    class TestViewModel(initialState: TestState) : MavericksViewModel<TestState>(initialState)

    class NonParcelableArgsFragment : Fragment(), MavericksView {
        override fun invalidate() {}
    }
}
