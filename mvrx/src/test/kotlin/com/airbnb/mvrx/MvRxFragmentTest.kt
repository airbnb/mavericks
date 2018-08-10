package com.airbnb.mvrx

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MvrxArgsTestArgs(val count: Int = 0) : Parcelable

class MvRxArgsFragment : BaseMvRxFragment() {



    override fun invalidate() {}
}