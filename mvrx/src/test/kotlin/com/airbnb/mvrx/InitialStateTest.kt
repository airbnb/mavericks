package com.airbnb.mvrx

import androidx.fragment.app.Fragment
import org.junit.Test

data class DefaultParamState(val foo: String = "") : MvRxState
class DefaultParamViewModel(initialState: DefaultParamState = DefaultParamState()) : TestMvRxViewModel<DefaultParamState>(initialState)
class NonDefaultParamViewModel(initialState: DefaultParamState) : TestMvRxViewModel<DefaultParamState>(initialState)

class InitialStateTest : BaseTest() {
    @Test(expected = IllegalArgumentException::class)
    fun testViewModelCantHaveDefaultState() {
        val (controller, fragment) = createFragment<Fragment, TestActivity>(args = ViewModelStoreTestArgs(3))

        MvRxViewModelProvider.get(
            DefaultParamViewModel::class.java,
            DefaultParamState::class.java,
            FragmentViewModelContext(controller.get(), null, fragment), "foo"
        )
    }

    @Test
    fun testViewModelCanBeCreated() {
        val (controller, fragment) = createFragment<Fragment, TestActivity>(args = ViewModelStoreTestArgs(3))

        MvRxViewModelProvider.get(
            NonDefaultParamViewModel::class.java,
            DefaultParamState::class.java,
            FragmentViewModelContext(controller.get(), null, fragment), "foo"
        )
    }
}
