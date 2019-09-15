package com.airbnb.mvrx

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.airbnb.mvrx.mock.EmptyMocks
import com.airbnb.mvrx.mock.MockableMvRxView
import com.airbnb.mvrx.mock.MvRxViewMocks

/**
 * Make your base Fragment class extend this to get MvRx functionality.
 *
 * This is necessary for the view model delegates and persistence to work correctly.
 */
abstract class BaseMvRxFragment : Fragment(), MockableMvRxView {

    override val mvrxViewModelStore by lazy { MvRxViewModelStore(viewModelStore) }

    private val mvrxViewIdProperty = MvRxViewId()
    final override val mvrxViewId: String by mvrxViewIdProperty

    override fun onCreate(savedInstanceState: Bundle?) {
        mvrxViewModelStore.restoreViewModels(this, savedInstanceState)
        mvrxViewIdProperty.restoreFrom(savedInstanceState)
        super.onCreate(savedInstanceState)
    }

    /**
     * Fragments should override the subscriptionLifecycle owner so that subscriptions made after onCreate
     * are properly disposed as fragments are moved from/to the backstack.
     */
    override val subscriptionLifecycleOwner: LifecycleOwner
        get() = this.viewLifecycleOwnerLiveData.value ?: this

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mvrxViewModelStore.saveViewModels(outState)
        mvrxViewIdProperty.saveTo(outState)
    }

    override fun onStart() {
        super.onStart()
        // This ensures that invalidate() is called for static screens that don't
        // subscribe to a ViewModel.
        postInvalidate()
    }

    override fun provideMocks(): MvRxViewMocks<out MockableMvRxView, out Parcelable> = EmptyMocks
}
