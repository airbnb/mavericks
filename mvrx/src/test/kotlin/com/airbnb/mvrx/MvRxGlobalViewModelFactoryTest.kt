package com.airbnb.mvrx

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.FragmentActivity
import kotlinx.parcelize.Parcelize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController

@Parcelize
data class MvRxTestArgs(val initialCount: Int = 1) : Parcelable

data class MavericksTestState(
    @PersistState val persistedCount: Int = 0,
    val notPersistedCount: Int = 0
) : MavericksState {
    @Suppress("unused")
    constructor(args: MvRxTestArgs) : this(persistedCount = args.initialCount, notPersistedCount = args.initialCount)
}

class MyViewModel(initialState: MavericksTestState) : TestMavericksViewModel<MavericksTestState>(initialState) {
    fun setCount(count: Int) = setState { copy(persistedCount = count, notPersistedCount = count) }
}

data class InvalidArgs(val initialCount: Int = 1)

data class InvalidState(val count: Int = 0) : MavericksState {
    @Suppress("unused")
    constructor(args: InvalidArgs) : this(count = args.initialCount)
}

class InvalidViewModel(initialState: InvalidState) : TestMavericksViewModel<InvalidState>(initialState)

class MvRxGlobalViewModelFactoryTest : BaseTest() {
    @get:Rule
    @Suppress("DEPRECATION")
    val thrown: ExpectedException = ExpectedException.none()

    @Test(expected = ViewModelDoesNotExistException::class)
    fun failForNonExistentViewModel() {
        @Suppress("DEPRECATION")
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

    @Test
    fun failForAccessingViewModelBeforeOnCreate() {
        thrown.apply {
            expect(IllegalStateException::class.java)
            expectMessage(ACCESSED_BEFORE_ON_CREATE_ERR_MSG)
        }

        val activityWithoutSetup = Robolectric.buildActivity(FragmentActivity::class.java).get()
        getViewModel(activityWithoutSetup)
    }

    @Test(expected = IllegalStateException::class)
    fun failForUnsupportedArgs() {
        val key = "invalid_vm"
        val vmClass = InvalidViewModel::class.java
        val (controller, activity) = buildActivity()

        MavericksViewModelProvider.get(
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
) = MavericksViewModelProvider.get(
    VmClass,
    MavericksTestState::class.java,
    ActivityViewModelContext(activity, MvRxTestArgs()),
    VmKey,
    forExistingViewModel
)

private fun buildActivity(savedInstanceState: Bundle? = null): Pair<ActivityController<FragmentActivity>, FragmentActivity> {
    val controller = Robolectric.buildActivity(FragmentActivity::class.java).apply {
        savedInstanceState?.let { setup(it) } ?: setup()
    }
    return controller to controller.get()
}

private const val VmKey = "key"
private val VmClass = MyViewModel::class.java
