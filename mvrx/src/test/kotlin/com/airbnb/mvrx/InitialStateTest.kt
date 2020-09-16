package com.airbnb.mvrx

import android.os.Parcelable
import androidx.fragment.app.Fragment
import kotlinx.android.parcel.Parcelize
import org.junit.Test

@Parcelize
data class ViewModelStoreTestArgs(val count: Int = 2) : Parcelable
data class DefaultParamState(val foo: String = "") : MavericksState
class DefaultParamViewModel(initialState: DefaultParamState = DefaultParamState()) : TestMavericksViewModel<DefaultParamState>(initialState)
class NonDefaultParamViewModel(initialState: DefaultParamState) : TestMavericksViewModel<DefaultParamState>(initialState)

class InitialStateTest : BaseTest() {
    @Test(expected = IllegalArgumentException::class)
    fun testViewModelCantHaveDefaultState() {
        val (controller, fragment) = createFragment<Fragment, TestActivity>(args = ViewModelStoreTestArgs(3))

        MavericksViewModelProvider.get(
            DefaultParamViewModel::class.java,
            DefaultParamState::class.java,
            FragmentViewModelContext(controller.get(), null, fragment), "foo"
        )
    }

    @Test
    fun testViewModelCanBeCreated() {
        val (controller, fragment) = createFragment<Fragment, TestActivity>(args = ViewModelStoreTestArgs(3))

        MavericksViewModelProvider.get(
            NonDefaultParamViewModel::class.java,
            DefaultParamState::class.java,
            FragmentViewModelContext(controller.get(), null, fragment), "foo"
        )
    }
}
