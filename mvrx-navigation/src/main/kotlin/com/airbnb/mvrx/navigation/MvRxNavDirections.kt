package com.airbnb.mvrx.navigation

import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.IdRes
import androidx.core.os.bundleOf
import androidx.navigation.NavDirections
import com.airbnb.mvrx.MvRx

/**
 * @param actionId - the navigation graph action ID.
 * @param data -  the a Parcelable MvRx that is passed as the [MvRx.KEY_ARG] fragment argument
 * @param title - an optional title that the destination can extract in the graph definition
 *
 * [title] usage examples
 *
 * ```xml
 *   <fragment
 *      android:id="@+id/financialAccountDetailsFragment"
 *      android:name="example.app.UserSettingsFragment"
 *      android:label="{mvrx:arg:title}">
 *
 *       <argument
 *          android:name="mvrx:arg:title"
 *          app:argType="string" />
 *
 *   </fragment>
 * ```
 */
class MvRxNavDirections(
    @IdRes private val actionId: Int,
    private val data: Parcelable,
    private val title: String? = null
) : NavDirections {

    companion object {
        const val KEY_ARG_TITLE = "mvrx:arg:title"
    }

    override fun getArguments(): Bundle =
        bundleOf(MvRx.KEY_ARG to data).apply {
            title?.also { putString(KEY_ARG_TITLE, it) }
        }

    @IdRes
    override fun getActionId(): Int = actionId
}
