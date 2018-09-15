package com.airbnb.mvrx

import android.support.v4.app.FragmentActivity
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.robolectric.Robolectric
import java.lang.reflect.InvocationTargetException

data class FactoryState(val count: Int = 0) : MvRxState
class TestFactoryViewModel(initialState: FactoryState, val otherProp: Long) : TestMvRxViewModel<FactoryState>(initialState) {
    companion object : MvRxViewModelFactory<FactoryState> {
        @JvmStatic
        override fun create(activity: FragmentActivity, state: FactoryState) = TestFactoryViewModel(state, 5)
    }
}

class FactoryTest : BaseTest() {

    private lateinit var activity: FragmentActivity

    @Before
    fun setup() {
        activity = Robolectric.setupActivity(FragmentActivity::class.java)
    }

    @Test
    fun createDefaultViewModel() {
        class MyViewModel(initialState: FactoryState) : TestMvRxViewModel<FactoryState>(initialState)
        val viewModel = MvRxViewModelProvider.get(MyViewModel::class.java, activity) { FactoryState() }
        withState(viewModel) { state ->
            assertEquals(FactoryState(), state)
        }
    }

    @Test
    fun createDefaultViewModelWithState() {
        class MyViewModel(initialState: FactoryState) : TestMvRxViewModel<FactoryState>(initialState)
        val viewModel = MvRxViewModelProvider.get(MyViewModel::class.java, activity) { FactoryState(count = 5) }
        withState(viewModel) { state ->
            assertEquals(FactoryState(count = 5), state)
        }
    }

    @Test
    fun createWithFactory() {
        val viewModel = MvRxViewModelProvider.get(TestFactoryViewModel::class.java, activity) { FactoryState(count = 5) }
        withState(viewModel) { state ->
            assertEquals(FactoryState(count = 5), state)
        }
        assertEquals(5, viewModel.otherProp)
    }

    private data class PrivateState(val count1: Int = 0) : MvRxState

    @Test(expected = InvocationTargetException::class)
    fun failOnPrivateState() {
        class MyViewModel(initialState: PrivateState) : TestMvRxViewModel<PrivateState>(initialState)
        // Create a view model to run state validation checks.
        @Suppress("UNUSED_VARIABLE")
        val viewModel = MvRxViewModelProvider.get(MyViewModel::class.java, activity) { PrivateState() }
    }

    @Test(expected = IllegalArgumentException::class)
    fun failOnDefaultState() {
        class MyViewModel(initialState: FactoryState = FactoryState()) : TestMvRxViewModel<FactoryState>(initialState)
        MvRxViewModelProvider.get(MyViewModel::class.java, activity) { FactoryState(count = 5) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun failOnWrongSingleParameterType() {
        class ViewModel : BaseMvRxViewModel<FactoryState>(initialState = FactoryState(), debugMode = false)
        MvRxViewModelProvider.get(ViewModel::class.java, activity) { FactoryState(count = 5) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun failOnMultipleParametersAndNoCompanion() {
        class OptionalParamViewModel(initialState: FactoryState, debugMode: Boolean = false) : BaseMvRxViewModel<FactoryState>(initialState, debugMode)
        MvRxViewModelProvider.get(OptionalParamViewModel::class.java, activity) { FactoryState(count = 5) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun failOnNoViewModelParameters() {
        class OptionalParamViewModel : BaseMvRxViewModel<FactoryState>(initialState = FactoryState(), debugMode = false)
        MvRxViewModelProvider.get(OptionalParamViewModel::class.java, activity) { FactoryState(count = 5) }
    }

    class TestFactoryViewModelNoJvmStatic(initialState: FactoryState, val otherProp: Long) : TestMvRxViewModel<FactoryState>(initialState) {
        companion object : MvRxViewModelFactory<FactoryState> {
            override fun create(activity: FragmentActivity, state: FactoryState) = TestFactoryViewModelNoJvmStatic(state, 5)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun failOnNoJvmStaticInCompanion() {
        MvRxViewModelProvider.get(TestFactoryViewModelNoJvmStatic::class.java, activity) { FactoryState(count = 5) }
    }
}