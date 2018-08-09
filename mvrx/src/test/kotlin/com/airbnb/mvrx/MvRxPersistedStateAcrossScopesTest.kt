package com.airbnb.mvrx

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelStore
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import kotlinx.android.parcel.Parcelize
import org.junit.Assert.assertEquals
import org.junit.Test
import org.robolectric.shadows.support.v4.SupportFragmentTestUtil

class MvRxPersistedStateAcrossScopesTest : MvRxBaseTest() {

    @Parcelize
    data class Args(val initialCount: Int) : Parcelable

    @Parcelize
    data class ParcelableClass(val argCount: Int = 0, @PersistState val persistedCount: Int = 0) : Parcelable, MvRxState {
        constructor(args: Args) : this(args.initialCount)
    }

    @Test
    fun testActivityScopedViewModelRestoredWithProperArgsAndPersistedState() {
        val fragment = PersistedStateScopedTestFragment()
        SupportFragmentTestUtil.startFragment(fragment, PersistedStateScopeTestActivity::class.java)
        val activity = fragment.requireActivity() as PersistedStateScopeTestActivity

        // Simulate args being sent in to the fragment
        val bundle = Bundle()
        bundle.putParcelable(MvRx.KEY_ARG, Args(1))
        fragment.arguments = bundle

        // Access the view model to add it to activity store
        val viewModel = fragment.viewModel
        viewModel.setPersistedCount(5)

        // Simulate the activity saving state
        val activitySaveInstanceState = Bundle()
        activity.mvrxViewModelStore.saveViewModels(activitySaveInstanceState)

        val activityStoreInNewProcess = MvRxViewModelStore(ViewModelStore())
        val outMap = mutableMapOf<String, ViewModel>()
        activityStoreInNewProcess.restoreViewModels(outMap, activity, activitySaveInstanceState)

        // Verify that both arg and persisted state are used to recreate state.
        val restoredViewModel = outMap["fragmentVM"] as PersistedStateScopeTestViewModel
        withState(restoredViewModel) { state ->
            assertEquals(1, state.argCount)
            assertEquals(5, state.persistedCount)
        }
    }
}

class PersistedStateScopeTestViewModel(override val initialState: MvRxPersistedStateAcrossScopesTest.ParcelableClass) :
    BaseMvRxViewModel<MvRxPersistedStateAcrossScopesTest.ParcelableClass>() {
    override val debugMode: Boolean = false

    fun setPersistedCount(count: Int) {
        setState { copy(persistedCount = count) }
    }
}

private class PersistedStateScopeTestActivity : FragmentActivity(), MvRxViewModelStoreOwner {

    private var store = lazy { MvRxViewModelStore(viewModelStore) }

    override val mvrxViewModelStore: MvRxViewModelStore
        get() = store.value
}

class PersistedStateScopedTestFragment : Fragment(), MvRxViewModelStoreOwner, MvRxView {

    val viewModel by activityViewModel(PersistedStateScopeTestViewModel::class, keyFactory = { "fragmentVM" })

    private var store = lazy { MvRxViewModelStore(viewModelStore) }

    override val mvrxViewModelStore: MvRxViewModelStore
        get() = store.value

    override fun readyToInvalidate(): Boolean = false

    override fun invalidate() {}
}
