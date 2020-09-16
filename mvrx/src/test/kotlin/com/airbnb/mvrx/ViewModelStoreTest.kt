package com.airbnb.mvrx

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

data class ViewModelStoreTestState(val notPersistedCount: Int = 1, @PersistState val persistedCount: Int = 1) : MavericksState {
    constructor(args: ViewModelStoreTestArgs) : this(args.count, args.count)
}

class ViewModelStoreTestViewModel(initialState: ViewModelStoreTestState) : TestMavericksViewModel<ViewModelStoreTestState>(initialState) {
    fun setCount(count: Int) = setState { copy(persistedCount = count, notPersistedCount = count) }
}

class ViewModelStoreActivity : TestActivity() {

    val viewModel by viewModel(ViewModelStoreTestViewModel::class)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_AppCompat_NoActionBar)
    }
}

@Suppress("DEPRECATION")
class ViewModelStoreTestFragment : Fragment(), MavericksView {
    val viewModelFragment: ViewModelStoreTestViewModel by fragmentViewModel()
    val viewModelActivity: ViewModelStoreTestViewModel by activityViewModel()

    override fun invalidate() {}
}

class ViewModelStoreTest : BaseTest() {

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
    fun testPersistedStateForActivityViewModelWhenSetFromFragmentArgs() {
        val (controller, fragment) = createFragment<ViewModelStoreTestFragment, TestMvRxActivity>(args = ViewModelStoreTestArgs(3))
        fragment.viewModelActivity.setCount(2)
        val bundle = Bundle()
        controller.saveInstanceState(bundle)
        val (_, fragment2) = createFragment<ViewModelStoreTestFragment, TestMvRxActivity>(savedInstanceState = bundle)
        withState(fragment2.viewModelActivity) { state ->
            assertEquals(3, state.notPersistedCount)
            assertEquals(2, state.persistedCount)
        }
    }

    @Test
    fun testActivityViewModelRetainedAcrossConfigurationChanges() {
        val (controller, fragment) = createFragment<ViewModelStoreTestFragment, TestMvRxActivity>()
        fragment.viewModelActivity.setCount(2)
        controller.configurationChange(Configuration().apply {
            setToDefaults()
            this.orientation = Configuration.ORIENTATION_LANDSCAPE
        })
        val recreatedFragment = controller.mvRxFragment<ViewModelStoreTestFragment>()
        assertNotEquals(fragment, recreatedFragment)
        withState(recreatedFragment.viewModelActivity) { state ->
            assertEquals(2, state.notPersistedCount)
            assertEquals(2, state.persistedCount)
        }
    }

    @Test
    @Config(qualifiers = "+port")
    fun testPersistedStateForActivityViewModelWhenSetFromFragmentArgsAfterConfigurationChange() {
        val (controller, fragment) = createFragment<ViewModelStoreTestFragment, TestMvRxActivity>(args = ViewModelStoreTestArgs(3))
        fragment.viewModelActivity.setCount(2)
        controller.configurationChange(Configuration().apply {
            setToDefaults()
            this.orientation = Configuration.ORIENTATION_LANDSCAPE
        })
        val bundleTwo = Bundle()
        controller.saveInstanceState(bundleTwo)

        val (_, fragment2) = createFragment<ViewModelStoreTestFragment, TestMvRxActivity>(savedInstanceState = bundleTwo)
        withState(fragment2.viewModelActivity) { state ->
            assertEquals(3, state.notPersistedCount)
            assertEquals(2, state.persistedCount)
        }
    }

    @Test
    fun testPersistedStateForFragmentViewModelWhenSetFromFragmentArgs() {
        val (controller, fragment) = createFragment<ViewModelStoreTestFragment, TestMvRxActivity>(args = ViewModelStoreTestArgs(3))
        fragment.viewModelFragment.setCount(2)
        val bundle = Bundle()
        controller.saveInstanceState(bundle)
        val (_, fragment2) = createFragment<ViewModelStoreTestFragment, TestMvRxActivity>(savedInstanceState = bundle)
        withState(fragment2.viewModelFragment) { state ->
            assertEquals(3, state.notPersistedCount)
            assertEquals(2, state.persistedCount)
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

    @Test
    fun testFragmentViewModelRetainedAcrossConfigurationChanges() {
        val (controller, fragment) = createFragment<ViewModelStoreTestFragment, TestMvRxActivity>()
        fragment.viewModelFragment.setCount(2)
        controller.configurationChange(Configuration().apply {
            setToDefaults()
            this.orientation = Configuration.ORIENTATION_LANDSCAPE
        })
        val recreatedFragment = controller.mvRxFragment<ViewModelStoreTestFragment>()
        assertNotEquals(fragment, recreatedFragment)
        withState(recreatedFragment.viewModelFragment) { state ->
            assertEquals(2, state.notPersistedCount)
            assertEquals(2, state.persistedCount)
        }
    }

    @Test
    @Config(qualifiers = "+port")
    fun testPersistedStateForFragmentViewModelWhenSetFromFragmentArgsAfterConfigurationChange() {
        val (controller, fragment) = createFragment<ViewModelStoreTestFragment, TestMvRxActivity>(args = ViewModelStoreTestArgs(3))
        fragment.viewModelFragment.setCount(2)
        controller.configurationChange(Configuration().apply {
            setToDefaults()
            this.orientation = Configuration.ORIENTATION_LANDSCAPE
        })
        val bundleTwo = Bundle()
        controller.saveInstanceState(bundleTwo)

        val (_, fragment2) = createFragment<ViewModelStoreTestFragment, TestMvRxActivity>(savedInstanceState = bundleTwo)
        withState(fragment2.viewModelFragment) { state ->
            assertEquals(3, state.notPersistedCount)
            assertEquals(2, state.persistedCount)
        }
    }

    @Test
    fun testViewModelInActivityWithoutArgs() {
        val controller = Robolectric.buildActivity(ViewModelStoreActivity::class.java).setup()
        withState(controller.get().viewModel) { state ->
            assertEquals(1, state.notPersistedCount)
            assertEquals(1, state.persistedCount)
        }
    }

    @Test
    fun testViewModelInActivityWithArgs() {
        val args = ViewModelStoreTestArgs(3)
        val intent = Intent()
        intent.putExtra(Mavericks.KEY_ARG, args)

        val controller = Robolectric.buildActivity(ViewModelStoreActivity::class.java, intent).setup()
        withState(controller.get().viewModel) { state ->
            assertEquals(3, state.notPersistedCount)
            assertEquals(3, state.persistedCount)
        }
    }

    @Test
    fun testViewModelInActivityWithSavedInstanceState() {
        val args = ViewModelStoreTestArgs(3)
        val intent = Intent()
        intent.putExtra(Mavericks.KEY_ARG, args)

        val controller = Robolectric.buildActivity(ViewModelStoreActivity::class.java, intent).setup()

        controller.get().viewModel.setCount(4)

        val savedInstanceState = Bundle()
        controller.saveInstanceState(savedInstanceState)

        val controller2 = Robolectric.buildActivity(ViewModelStoreActivity::class.java, intent).setup(savedInstanceState)

        withState(controller2.get().viewModel) { state ->
            assertEquals(3, state.notPersistedCount)
            assertEquals(4, state.persistedCount)
        }
    }
}
