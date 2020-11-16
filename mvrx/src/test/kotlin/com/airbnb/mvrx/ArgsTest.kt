package com.airbnb.mvrx

import android.os.Parcelable
import androidx.fragment.app.Fragment
import kotlinx.android.parcel.Parcelize
import org.junit.Assert
import org.junit.Test

@Parcelize
data class MvrxArgsTestArgs(val count: Int = 0) : Parcelable

@Parcelize
data class MvrxArgsTestArgs2(val count: Int = 0) : Parcelable

class MvRxArgsFragment : Fragment(), MavericksView {
    val args: MvrxArgsTestArgs by args()

    override fun invalidate() {}
}

class MvRxFragmentTest : BaseTest() {
    @Test
    fun testArgs() {
        val (_, fragment) = createFragment<MvRxArgsFragment, TestMvRxActivity>(args = MvrxArgsTestArgs())
        Assert.assertEquals(0, fragment.args.count)
    }

    @Test
    fun testSetArgs() {
        val (_, fragment) = createFragment<MvRxArgsFragment, TestMvRxActivity>(args = MvrxArgsTestArgs(2))
        Assert.assertEquals(2, fragment.args.count)
    }

    @Test(expected = ClassCastException::class)
    fun testSetWrongArgs() {
        val (_, fragment) = createFragment<MvRxArgsFragment, TestMvRxActivity>(args = MvrxArgsTestArgs2(2))
        fragment.args
    }

    @Test(expected = IllegalArgumentException::class)
    fun testNoArgs() {
        val (_, fragment) = createFragment<MvRxArgsFragment, TestMvRxActivity>()
        fragment.args
    }
}
