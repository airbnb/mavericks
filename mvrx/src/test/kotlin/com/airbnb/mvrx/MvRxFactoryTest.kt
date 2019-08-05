package com.airbnb.mvrx

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.parcel.Parcelize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController
import java.lang.IllegalStateException

@Parcelize
data class MvRxFactoryArgs(val initialCount: Int = 1) : Parcelable

data class MvRxFactoryState(
    @PersistState val persistedCount: Int = 0,
    val notPersistedCount: Int = 0
) : MvRxState {
    constructor(args: MvRxFactoryArgs) : this(persistedCount = args.initialCount, notPersistedCount = args.initialCount)
}

class MyViewModel(initialState: MvRxFactoryState) : TestMvRxViewModel<MvRxFactoryState>(initialState) {
    fun setCount(count: Int) = setState { copy(persistedCount = count, notPersistedCount = count) }
}

data class InvalidArgs(val initialCount: Int = 1)

data class InvalidState(val count: Int = 0) : MvRxState {
    constructor(args: InvalidArgs) : this(count = args.initialCount)
}

class InvalidViewModel(initialState: InvalidState) : TestMvRxViewModel<InvalidState>(initialState)

class MvRxFactoryTest : BaseTest() {

    @Test(expected = ViewModelDoesNotExistException::class)
    fun failForNonExistentViewModel() {
        val activity = Robolectric.setupActivity(FragmentActivity::class.java)

        val factory = createMvRxFactory(activity, forExistingViewModel = true)
        getViewModel(activity, factory)
    }

    @Test
    fun createFromRestoredInstanceState() {
        val (controller, activity) = buildActivity()

        val factory = createMvRxFactory(activity)

        val viewModel = getViewModel(activity, factory)
        viewModel.setCount(3)

        val bundle = Bundle()
        controller.saveInstanceState(bundle)

        val (_, recreatedActivity) = buildActivity(savedInstanceState = bundle)

        val recreatedFactory = createMvRxFactory(recreatedActivity)
        val recreatedViewModel = getViewModel(recreatedActivity, recreatedFactory)

        assertNotEquals(viewModel, recreatedViewModel)

        withState(recreatedViewModel) {
            assertEquals(it.persistedCount, 3)
            assertEquals(it.notPersistedCount, 1)
        }
    }

    @Test
    fun createExistingFromRestoredInstanceState() {
        val (controller, activity) = buildActivity()

        val factory = createMvRxFactory(activity)

        val viewModel = getViewModel(activity, factory)
        viewModel.setCount(3)

        val bundle = Bundle()
        controller.saveInstanceState(bundle)

        val (_, recreatedActivity) = buildActivity(savedInstanceState = bundle)

        val recreatedFactory = createMvRxFactory(recreatedActivity, forExistingViewModel = true)
        val recreatedViewModel = getViewModel(recreatedActivity, recreatedFactory)

        assertNotEquals(viewModel, recreatedViewModel)

        withState(recreatedViewModel) {
            assertEquals(it.persistedCount, 3)
            assertEquals(it.notPersistedCount, 1)
        }
    }

    @Test(expected = IllegalStateException::class)
    fun failForUnsupportedArgs() {
        val key = "invalid_vm"
        val vmClass = InvalidViewModel::class.java
        val (controller, activity) = buildActivity()

        ViewModelProviders.of(
            activity,
            MvRxFactory(
                vmClass,
                InvalidState::class.java,
                ActivityViewModelContext(activity, InvalidArgs()),
                key
            )
        ).get(key, vmClass)
        controller.saveInstanceState(Bundle())
    }

    private fun createMvRxFactory(
        activity: FragmentActivity,
        forExistingViewModel: Boolean = false
    ) = MvRxFactory(
        VM_CLASS,
        MvRxFactoryState::class.java,
        ActivityViewModelContext(activity, MvRxFactoryArgs()),
        VM_KEY,
        forExistingViewModel
    )
}

private fun getViewModel(
    activity: FragmentActivity,
    factory: ViewModelProvider.Factory
) = ViewModelProviders.of(activity, factory).get(VM_KEY, VM_CLASS)

private fun buildActivity(savedInstanceState: Bundle? = null): Pair<ActivityController<FragmentActivity>, FragmentActivity> {
    val controller = Robolectric.buildActivity(FragmentActivity::class.java).apply {
        savedInstanceState?.let { setup(it) } ?: setup()
    }
    return controller to controller.get()
}

private const val VM_KEY = "key"
private val VM_CLASS = MyViewModel::class.java