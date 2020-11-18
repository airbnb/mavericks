package com.airbnb.mvrx.navigation

import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.IdRes
import androidx.navigation.NavDirections
import com.airbnb.mvrx.Mavericks
import java.io.Serializable

/**
 * [MvRxNavDirections.create] must be used to pick between a [Parcelable] or [Serializable]
 * data argument.
 *
 * @param actionId - the navigation graph action ID.
 * @param parcelable -  A [Parcelable] MvRx argument that is passed as the [Mavericks.KEY_ARG] fragment argument
 * @param serializable -  A [Serializable] MvRx argument that is passed as the [Mavericks.KEY_ARG] fragment argument
 * @param title - an optional title that the destination can extract in the graph definition
 *
 * Graph usage example.
 *
 * ```xml
 *   <fragment
 *      android:id="@+id/financialAccountDetailsFragment"
 *      android:name="example.app.UserSettingsFragment"
 *      android:label="{mvrx:arg:title}">
 *
 *       <argument
 *          android:name="mvrx:arg"
 *          app:argType="com.example.ExampleNavArgs" />
 *
 *       <argument
 *          android:name="mvrx:arg:title"
 *          app:argType="string" />
 *
 *   </fragment>
 * ```
 */
class MvRxNavDirections private constructor(
    @IdRes private val actionId: Int,
    private val parcelable: Parcelable? = null,
    private val serializable: Serializable? = null,
    private val title: String? = null
) : NavDirections {

    companion object {
        const val KEY_ARG_TITLE = "mvrx:arg:title"
        fun create(@IdRes actionId: Int, title: String?, data: Parcelable): MvRxNavDirections =
            MvRxNavDirections(
                actionId = actionId,
                title = title,
                parcelable = data
            )

        fun create(@IdRes actionId: Int, title: String?, data: Serializable): MvRxNavDirections =
            MvRxNavDirections(
                actionId = actionId,
                title = title,
                serializable = data
            )
    }

    override fun getArguments(): Bundle =
        Bundle().apply {
            if (parcelable != null) {
                putParcelable(Mavericks.KEY_ARG, parcelable)
            } else {
                putSerializable(Mavericks.KEY_ARG, serializable!!)
            }
            title?.also { putString(KEY_ARG_TITLE, it) }
        }

    @IdRes
    override fun getActionId(): Int = actionId
}
