package com.airbnb.mvrx

import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import kotlinx.android.parcel.Parcelize
import org.junit.Assert.assertEquals
import org.junit.Test
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController

@Parcelize
data class ViewModelStoreTestArgs(val foo: String) : Parcelable

data class ViewModelStoreTestState(val foo: String = "Default") : MvRxState {
    constructor(args: ViewModelStoreTestArgs) : this(foo = args.foo)
}

class ViewModelStoreTestViewModel(override val initialState: ViewModelStoreTestState) : TestMvRxViewModel<ViewModelStoreTestState>()

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

class TestActivity : MvRxActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_AppCompat_NoActionBar)
    }
}

class ViewModelStoreTestFragment : MvRxFragment() {
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
            assertEquals("Default", state.foo)
        }
    }

    @Test
    fun testFragmentViewModelCanUseDefaultConstructor() {
        val (_, fragment) = createFragment<ViewModelStoreTestFragment, TestActivity>()
        withState(fragment.viewModelFragment) { state ->
            assertEquals("Default", state.foo)
        }
    }

    @Test
    fun testActivityViewModelCanBeSetFromArgs() {
        val (_, fragment) = createFragment<ViewModelStoreTestFragment, TestActivity>(args = ViewModelStoreTestArgs("From Args"))
        withState(fragment.viewModelActivity) { state ->
            assertEquals("From Args", state.foo)
        }
    }

    @Test
    fun testFragmentViewModelCanBeSetFromArgs() {
        val (_, fragment) = createFragment<ViewModelStoreTestFragment, TestActivity>(args = ViewModelStoreTestArgs("From Args"))
        withState(fragment.viewModelFragment) { state ->
            assertEquals("From Args", state.foo)
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

    private inline fun <reified T : Fragment, reified A : AppCompatActivity> createFragment(
            savedInstanceState: Bundle? = null,
            args: Parcelable? = null
    ): Pair<ActivityController<A>, T> {
        val controller = Robolectric.buildActivity(A::class.java)
                .create(savedInstanceState)
                .start()
                .resume()
                .visible()
        val activity = controller.get()

        val fragment = T::class.java.newInstance().apply {
            arguments = Bundle().apply { putParcelable(MvRx.KEY_ARG, args) }
        }

        activity.supportFragmentManager.beginTransaction().add(fragment, "TAG").commitNow()
        return controller to fragment
    }
}