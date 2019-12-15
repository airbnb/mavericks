package com.airbnb.mvrx.navigation

import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.IdRes
import androidx.core.os.bundleOf
import androidx.navigation.NavDirections
import com.airbnb.mvrx.MvRx

class MvRxNavDirections(
    @IdRes private val actionId: Int,
    private val data: Parcelable
) : NavDirections {

    override fun getArguments(): Bundle =
        bundleOf(MvRx.KEY_ARG to data)

    @IdRes
    override fun getActionId(): Int = actionId
}
