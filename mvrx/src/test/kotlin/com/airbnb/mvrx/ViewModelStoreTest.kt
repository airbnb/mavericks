package com.airbnb.mvrx

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.app.AppCompatActivity
import kotlinx.android.parcel.Parcelize
import org.junit.Assert.assertEquals
import org.junit.Test
import org.robolectric.Robolectric

@Parcelize
data class ViewModelStoreTestArgs(val count: Int = 2) : Parcelable

data class ViewModelStoreTestState(val notPersistedCount: Int = 1, @PersistState val persistedCount: Int = 1) : MvRxState {
    constructor(args: ViewModelStoreTestArgs) : this(args.count, args.count)
}

class ViewModelStoreTestViewModel(initialState: ViewModelStoreTestState) : TestMvRxViewModel<ViewModelStoreTestState>(initialState) {
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

class ViewModelStoreActivity : TestActivity() {

    val viewModel by viewModel(ViewModelStoreTestViewModel::class)

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

class ViewModelStoreTest : BaseTest() {

    @Test
    fun testCanCreateFragment() {
        val (_, fragment) = createFragment<ViewModelStoreTestFragment, TestActivity>()

        var callCount = 0
        withAsyncState(fragment.viewModelFragment) { callCount++ }
        withAsyncState(fragment.viewModelFragment) { callCount++ }
        assertEquals(2, callCount)
    }

    @Test
    fun testActivityViewModelCanUseDefaultConstructor() {
        val (_, fragment) = createFragment<ViewModelStoreTestFragment, TestActivity>()
        withAsyncState(fragment.viewModelActivity) { state ->
            assertEquals(1, state.notPersistedCount)
        }
    }

    @Test
    fun testFragmentViewModelCanUseDefaultConstructor() {
        val (_, fragment) = createFragment<ViewModelStoreTestFragment, TestActivity>()
        withAsyncState(fragment.viewModelFragment) { state ->
            assertEquals(1, state.notPersistedCount)
        }
    }

    @Test
    fun testActivityViewModelCanBeSetFromArgs() {
        val (_, fragment) = createFragment<ViewModelStoreTestFragment, TestActivity>(args = ViewModelStoreTestArgs(3))
        withAsyncState(fragment.viewModelActivity) { state ->
            assertEquals(3, state.notPersistedCount)
        }
    }

    @Test
    fun testFragmentViewModelCanBeSetFromArgs() {
        val (_, fragment) = createFragment<ViewModelStoreTestFragment, TestActivity>(args = ViewModelStoreTestArgs(3))
        withAsyncState(fragment.viewModelFragment) { state ->
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
        withAsyncState(fragment2.viewModelActivity) { state ->
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
        withAsyncState(fragment2.viewModelActivity) { state ->
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
        withAsyncState(fragment2.viewModelFragment) { state ->
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
        withAsyncState(fragment2.viewModelFragment) { state ->
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

    @Test
    fun testViewModelInActivityWithoutArgs() {
        val controller = Robolectric.buildActivity(ViewModelStoreActivity::class.java)
                .create(null)
                .start()
                .resume()
                .visible()
        withAsyncState(controller.get().viewModel) { state ->
            assertEquals(1, state.notPersistedCount)
            assertEquals(1, state.persistedCount)
        }
    }

    @Test
    fun testViewModelInActivityWithArgs() {
        val args = ViewModelStoreTestArgs(3)
        val intent = Intent()
        intent.putExtra(MvRx.KEY_ARG, args)

        val controller = Robolectric.buildActivity(ViewModelStoreActivity::class.java, intent)
                .create(null)
                .start()
                .resume()
                .visible()
        withAsyncState(controller.get().viewModel) { state ->
            assertEquals(3, state.notPersistedCount)
            assertEquals(3, state.persistedCount)
        }
    }

    @Test
    fun testViewModelInActivityWithSavedInstanceState() {
        val args = ViewModelStoreTestArgs(3)
        val intent = Intent()
        intent.putExtra(MvRx.KEY_ARG, args)

        val controller = Robolectric.buildActivity(ViewModelStoreActivity::class.java, intent)
                .create(null)
                .start()
                .resume()
                .visible()

        controller.get().viewModel.setCount(4)

        val savedInstanceState = Bundle()
        controller.saveInstanceState(savedInstanceState)


        val controller2 = Robolectric.buildActivity(ViewModelStoreActivity::class.java, intent)
                .create(savedInstanceState)
                .start()
                .resume()
                .visible()

        withAsyncState(controller2.get().viewModel) { state ->
            assertEquals(3, state.notPersistedCount)
            assertEquals(4, state.persistedCount)
        }
    }
}