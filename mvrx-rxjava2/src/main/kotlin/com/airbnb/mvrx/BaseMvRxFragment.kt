package com.airbnb.mvrx

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

/**
 * Make your base Fragment class extend this to get MvRx functionality.
 *
 * This is necessary for the view model delegates and persistence to work correctly.
 */
@Deprecated("You no longer need a base MvRxFragment. All you need to do is make your Fragment implement MvRxView.")
abstract class BaseMvRxFragment(@LayoutRes contentLayoutId: Int = 0) : Fragment(contentLayoutId), MvRxView
