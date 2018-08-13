package com.airbnb.mvrx

import android.support.v4.app.FragmentActivity
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.robolectric.Robolectric

data class FactoryState(val count: Int = 0) : MvRxState
class TestFactoryViewModel(override val initialState: FactoryState, val otherProp: Long) : TestMvRxViewModel<FactoryState>() {
    companion object : MvRxViewModelFactory<FactoryState> {
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
        class MyViewModel(override val initialState: FactoryState) : TestMvRxViewModel<FactoryState>()
        val viewModel = MvRxViewModelProvider.get(MyViewModel::class, activity) { FactoryState() }
        withState(viewModel) { state ->
            assertEquals(FactoryState(), state)
        }
    }

    @Test
    fun createDefaultViewModelWithState() {
        class MyViewModel(override val initialState: FactoryState) : TestMvRxViewModel<FactoryState>()
        val viewModel = MvRxViewModelProvider.get(MyViewModel::class, activity) { FactoryState(count = 5) }
        withState(viewModel) { state ->
            assertEquals(FactoryState(count = 5), state)
        }
    }

    @Test
    fun createWithFactory() {
        val viewModel = MvRxViewModelProvider.get(TestFactoryViewModel::class, activity) { FactoryState(count = 5) }
        withState(viewModel) { state ->
            assertEquals(FactoryState(count = 5), state)
        }
        assertEquals(5, viewModel.otherProp)
    }

    private data class PrivateState(val count1: Int = 0) : MvRxState
    @Test(expected = IllegalStateException::class)
    fun failOnPrivateState() {
        class MyViewModel(override val initialState: PrivateState) : TestMvRxViewModel<PrivateState>()
        val viewModel = MvRxViewModelProvider.get(MyViewModel::class, activity) { PrivateState() }
        viewModel.validateState()
    }

    @Test(expected = IllegalArgumentException::class)
    fun failOnDefaultState() {
        class MyViewModel(override val initialState: FactoryState = FactoryState()) : TestMvRxViewModel<FactoryState>()
        MvRxViewModelProvider.get(MyViewModel::class, activity) { FactoryState(count = 5) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun failOnNoStateInConstructor() {
        class MyViewModel : TestMvRxViewModel<FactoryState>() {
            override val initialState = FactoryState()
        }
        MvRxViewModelProvider.get(MyViewModel::class, activity) { FactoryState(count = 5) }
    }
}