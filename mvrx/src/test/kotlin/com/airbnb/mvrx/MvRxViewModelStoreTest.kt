package com.airbnb.mvrx

import android.os.Bundle
import android.os.Parcelable
import android.support.v7.app.AppCompatActivity
import kotlinx.android.parcel.Parcelize
import org.junit.Assert.assertEquals
import org.junit.Test

@Parcelize
data class ViewModelStoreTestArgs(val count: Int = 2) : Parcelable

data class ViewModelStoreTestState(val notPersistedCount: Int = 1, @PersistState val persistedCount: Int = 1) : MvRxState {
    constructor(args: ViewModelStoreTestArgs) : this(args.count, args.count)
}

class ViewModelStoreTestViewModel(override val initialState: ViewModelStoreTestState) : TestMvRxViewModel<ViewModelStoreTestState>() {
    fun setCount(count: Int) = setState { copy(persistedCount = count, notPersistedCount = count) }
}

class NoRestoreActivity : AppCompatActivity(), MvRxViewModelStoreOwner {
    override val mvrxViewModelStore by lazy { MvRxViewModelStore(viewModelStore) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_AppCompat_NoActionBar)
    }
}

class NoSaveActivity : AppCompatActivity(), MvRxViewModelStoreOwner {
    override val mvrxViewModelStore by lazy { MvRxViewModelStore(viewModelStore) }

    override fun onCreate(savedInstanceState: Bundle?) {
        mvrxViewModelStore.restoreViewModels(this, savedInstanceState)
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_AppCompat_NoActionBar)
    }
}

class TestActivity : BaseMvRxActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_AppCompat_NoActionBar)
    }
}

class ViewModelStoreTestFragment : BaseMvRxFragment() {
    val viewModelFragment by fragmentViewModel(ViewModelStoreTestViewModel::class)
    val viewModelActivity by activityViewModel(ViewModelStoreTestViewModel::class)

    override fun invalidate() { }
}

class MvRxViewModelStoreTest : MvRxBaseTest() {

    @Test
    fun testCanCreateFragment() {
        val (_, fragment) = createFragment<ViewModelStoreTestFragment, TestActivity>()

        var callCount = 0
        withState(fragment.viewModelFragment) { callCount++ }
        withState(fragment.viewModelFragment) { callCount++ }
        assertEquals(2, callCount)
    }

    @Test
    fun testActivityViewModelCanUseDefaultConstructor() {
        val (_, fragment) = createFragment<ViewModelStoreTestFragment, TestActivity>()
        withState(fragment.viewModelActivity) { state ->
            assertEquals(1, state.notPersistedCount)
        }
    }

    @Test
    fun testFragmentViewModelCanUseDefaultConstructor() {
        val (_, fragment) = createFragment<ViewModelStoreTestFragment, TestActivity>()
        withState(fragment.viewModelFragment) { state ->
            assertEquals(1, state.notPersistedCount)
        }
    }

    @Test
    fun testActivityViewModelCanBeSetFromArgs() {
        val (_, fragment) = createFragment<ViewModelStoreTestFragment, TestActivity>(args = ViewModelStoreTestArgs(3))
        withState(fragment.viewModelActivity) { state ->
            assertEquals(3, state.notPersistedCount)
        }
    }

    @Test
    fun testFragmentViewModelCanBeSetFromArgs() {
        val (_, fragment) = createFragment<ViewModelStoreTestFragment, TestActivity>(args = ViewModelStoreTestArgs(3))
        withState(fragment.viewModelFragment) { state ->
            assertEquals(3, state.notPersistedCount)
        }
    }


    @Test
    fun testPersistedStateForActivityViewModelWhenSetFromFragmentArgs() {
        val (controller, fragment) = createFragment<ViewModelStoreTestFragment, TestMvRxActivity>(args = ViewModelStoreTestArgs(3))
        fragment.viewModelActivity
        val bundle = Bundle()
        controller.saveInstanceState(bundle)
        val (_, fragment2) = createFragment<ViewModelStoreTestFragment, TestMvRxActivity>(savedInstanceState = bundle)
        withState(fragment2.viewModelActivity) { state ->
            assertEquals(3, state.notPersistedCount)
            assertEquals(3, state.persistedCount)
        }
    }

    @Test
    fun testPersistedStateForActivityViewModel() {
        val (controller, fragment) = createFragment<ViewModelStoreTestFragment, TestMvRxActivity>()
        fragment.viewModelActivity.setCount(3)
        val bundle = Bundle()
        controller.saveInstanceState(bundle)
        val (_, fragment2) = createFragment<ViewModelStoreTestFragment, TestMvRxActivity>(savedInstanceState = bundle)
        withState(fragment2.viewModelActivity) { state ->
            assertEquals(1, state.notPersistedCount)
            assertEquals(3, state.persistedCount)
        }
    }

    @Test
    fun testPersistedStateForFragmentViewModelWhenSetFromFragmentArgs() {
        val (controller, fragment) = createFragment<ViewModelStoreTestFragment, TestMvRxActivity>(args = ViewModelStoreTestArgs(3))
        fragment.viewModelFragment
        val bundle = Bundle()
        controller.saveInstanceState(bundle)
        val (_, fragment2) = createFragment<ViewModelStoreTestFragment, TestMvRxActivity>(savedInstanceState = bundle)
        withState(fragment2.viewModelFragment) { state ->
            assertEquals(3, state.notPersistedCount)
            assertEquals(3, state.persistedCount)
        }
    }

    @Test
    fun testPersistedStateForFragmentViewModel() {
        val (controller, fragment) = createFragment<ViewModelStoreTestFragment, TestMvRxActivity>()
        fragment.viewModelFragment.setCount(3)
        val bundle = Bundle()
        controller.saveInstanceState(bundle)
        val (_, fragment2) = createFragment<ViewModelStoreTestFragment, TestMvRxActivity>(savedInstanceState = bundle)
        withState(fragment2.viewModelFragment) { state ->
            assertEquals(1, state.notPersistedCount)
            assertEquals(3, state.persistedCount)
        }
    }

    @Test(expected = IllegalStateException::class)
    fun testNoRestoreInActivityCrashes() {
        val (_, fragment) = createFragment<ViewModelStoreTestFragment, NoRestoreActivity>()
        fragment.viewModelActivity
    }

    @Test(expected = IllegalStateException::class)
    fun testNoSaveInActivityCrashes() {
        val (controller, fragment) = createFragment<ViewModelStoreTestFragment, NoSaveActivity>()
        fragment.viewModelActivity
        val bundle = Bundle()
        controller.saveInstanceState(bundle)
        createFragment<ViewModelStoreTestFragment, NoSaveActivity>(savedInstanceState = bundle)
    }
}