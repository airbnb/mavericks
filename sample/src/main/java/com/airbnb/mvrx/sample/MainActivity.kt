package com.airbnb.mvrx.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.airbnb.mvrx.MvRxViewModelStore
import com.airbnb.mvrx.MvRxViewModelStoreOwner

/**
 * Extend this class to get MvRx support out of the box.
 *
 * The purpose of this class is to:
 * 1) Be the host of MvRxFragments. MvRxFragments are the screen unit in MvRx. Activities are meant
 *    to just be the shell for your Fragments. There should be no business logic in your
 *    Activities anymore. Use activityViewModel to share state between screens.
 * 2) Properly configure MvRx so it has things like the correct ViewModelStore.
 *
 * To integrate this into your app. you may:
 * 1) Extend this directly.
 * 2) Replace your BaseActivity super class with this one.
 * 3) Manually integrate this into your base Activity (not recommended).
 */
class MainActivity : AppCompatActivity(), MvRxViewModelStoreOwner {

    /**
     * MvRx has its own wrapped ViewModelStore that enables improved state restoration if Android
     * kills your app and restores it in a new process.
     */
    override val mvrxViewModelStore by lazy { MvRxViewModelStore(viewModelStore) }

    override fun onCreate(savedInstanceState: Bundle?) {
        /**
         * This MUST be called before super!
         * In a new process, super.onCreate will trigger Fragment.onCreate, which could access a ViewModel.
         */
        mvrxViewModelStore.restoreViewModels(this, savedInstanceState)
        super.onCreate(savedInstanceState)
        /**
         * Set the content view to a container that can be used to host MvRxFragments.
         * No other views should live at the Activity level.
         */
        setContentView(R.layout.activity_main)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mvrxViewModelStore.saveViewModels(outState)
    }
}