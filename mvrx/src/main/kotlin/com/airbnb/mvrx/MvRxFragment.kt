package com.airbnb.mvrx

import android.os.Bundle
import android.support.v4.app.Fragment

abstract class MvRxFragment : Fragment(), MvRxView {

    override val mvrxViewModelStore by lazy { MvRxViewModelStore(viewModelStore) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /**
         * This MUST be done to restore ViewModel state.
         */
        mvrxViewModelStore.restoreViewModels(this, savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mvrxViewModelStore.saveViewModels(outState)
    }

    /**
     * TODO: Remove this
     */
    override fun readyToInvalidate() = isAdded && view != null
}