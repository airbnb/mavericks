package com.airbnb.mvrx

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner

/**
 * Make your base Fragment class extend this to get MvRx functionality.
 *
 * This is necessary for the view model delegates and persistence to work correctly.
 */
abstract class BaseMvRxFragment(@LayoutRes contentLayoutId: Int = 0) : Fragment(contentLayoutId), MvRxView {

    private lateinit var viewDelegate: DelegateMvRxView

    final override val mvrxViewId: String get() = viewDelegate.mvrxViewId

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewDelegate = DelegateMvRxView(this, ::invalidate)
    }

    override val subscriptionLifecycleOwner: LifecycleOwner
        get() = this.viewLifecycleOwnerLiveData.value ?: this

    override fun uniqueOnly(customId: String?): UniqueOnly = viewDelegate.uniqueOnly(customId)

    override fun invalidate() = Unit

    override fun postInvalidate() = viewDelegate.postInvalidate()
}
