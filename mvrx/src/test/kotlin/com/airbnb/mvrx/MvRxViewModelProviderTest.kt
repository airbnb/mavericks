package com.airbnb.mvrx

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.FragmentActivity
import kotlinx.android.parcel.Parcelize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController
import java.lang.IllegalStateException

@Parcelize
data class MvRxTestArgs(val initialCount: Int = 1) : Parcelable

data class MvRxTestState(
    @PersistState val persistedCount: Int = 0,
    val notPersistedCount: Int = 0
) : MvRxState {
    constructor(args: MvRxTestArgs) : this(persistedCount = args.initialCount, notPersistedCount = args.initialCount)
}

class MyViewModel(initialState: MvRxTestState) : TestMvRxViewModel<MvRxTestState>(initialState) {
    fun setCount(count: Int) = setState { copy(persistedCount = count, notPersistedCount = count) }
}

data class InvalidArgs(val initialCount: Int = 1)

data class InvalidState(val count: Int = 0) : MvRxState {
    constructor(args: InvalidArgs) : this(count = args.initialCount)
}

class InvalidViewModel(initialState: InvalidState) : TestMvRxViewModel<InvalidState>(initialState)

class MvRxViewModelProviderTest : BaseTest() {

    @Test(expected = ViewModelDoesNotExistException::class)
    fun failForNonExistentViewModel() {
        val activity = Robolectric.setupActivity(FragmentActivity::class.java)
        getViewModel(activity, forExistingViewModel = true)
    }

    @Test
    fun createFromRestoredInstanceState() {
        val (controller, activity) = buildActivity()

        val viewModel = getViewModel(activity)
        viewModel.setCount(3)

        val bundle = Bundle()
        controller.saveInstanceState(bundle)

        val (_, recreatedActivity) = buildActivity(savedInstanceState = bundle)

        val recreatedViewModel = getViewModel(recreatedActivity)

        assertNotEquals(viewModel, recreatedViewModel)

        withState(recreatedViewModel) {
            assertEquals(it.persistedCount, 3)
            assertEquals(it.notPersistedCount, 1)
        }
    }

    @Test
    fun createExistingFromRestoredInstanceState() {
        val (controller, activity) = buildActivity()

        val viewModel = getViewModel(activity)
        viewModel.setCount(3)

        val bundle = Bundle()
        controller.saveInstanceState(bundle)

        val (_, recreatedActivity) = buildActivity(savedInstanceState = bundle)

        val recreatedViewModel = getViewModel(recreatedActivity, forExistingViewModel = true)

        assertNotEquals(viewModel, recreatedViewModel)

        withState(recreatedViewModel) {
            assertEquals(it.persistedCount, 3)
            assertEquals(it.notPersistedCount, 1)
        }
    }

    @Test(expected = IllegalStateException::class)
    fun failForAccessingViewModelBeforeOnCreate() {
        val activityWithoutSetup = Robolectric.buildActivity(FragmentActivity::class.java).get()
        getViewModel(activityWithoutSetup)
    }

    @Test(expected = IllegalStateException::class)
    fun failForUnsupportedArgs() {
        val key = "invalid_vm"
        val vmClass = InvalidViewModel::class.java
        val (controller, activity) = buildActivity()

        MvRxViewModelProvider.get(
            vmClass,
            InvalidState::class.java,
            ActivityViewModelContext(activity, InvalidArgs()),
            key
        )
        controller.saveInstanceState(Bundle())
    }
}

private fun getViewModel(
    activity: FragmentActivity,
    forExistingViewModel: Boolean = false
) = MvRxViewModelProvider.get(
    VM_CLASS,
    MvRxTestState::class.java,
    ActivityViewModelContext(activity, MvRxTestArgs()),
    VM_KEY,
    forExistingViewModel
)

private fun buildActivity(savedInstanceState: Bundle? = null): Pair<ActivityController<FragmentActivity>, FragmentActivity> {
    val controller = Robolectric.buildActivity(FragmentActivity::class.java).apply {
        savedInstanceState?.let { setup(it) } ?: setup()
    }
    return controller to controller.get()
}

private const val VM_KEY = "key"
private val VM_CLASS = MyViewModel::class.java