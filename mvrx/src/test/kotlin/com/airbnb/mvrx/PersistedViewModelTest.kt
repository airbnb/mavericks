package com.airbnb.mvrx

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelStore
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

enum class TestEnum { A, B, C }

class PersistedViewModelTest : BaseTest() {

    private lateinit var store: MvRxViewModelStore
    private lateinit var activity: FragmentActivity

    @Before
    fun setup() {
        store = MvRxViewModelStore(ViewModelStore())
        activity = mock(FragmentActivity::class.java)
    }

    // All data classes need to be declared in a public scope, otherwise kotlin will get smart about optimizing the generated code
    // and remove the no-arg java constructor.
    data class SetSaveIntState(@PersistState val count: Int = 0) : MvRxState
    @Test fun setSaveInt() {
        class TestViewModel(override val initialState: SetSaveIntState) : TestMvRxViewModel<SetSaveIntState>()
        val originalState = SetSaveIntState(count = 7)
        val viewModel = TestViewModel(originalState)
        val newState = testViewModel(viewModel)
        assertEquals(7, newState.count)
    }

    data class SetSaveEnumState(@PersistState val enumVal: TestEnum = TestEnum.A) : MvRxState
    @Test fun setSaveEnum() {
        class TestViewModel(override val initialState: SetSaveEnumState) : TestMvRxViewModel<SetSaveEnumState>()
        val originalState = SetSaveEnumState(enumVal = TestEnum.C)
        val viewModel = TestViewModel(originalState)
        val newState = testViewModel(viewModel)
        assertEquals(TestEnum.C, newState.enumVal)
    }

    data class SetSaveOneIntState(@PersistState val count1: Int = 0, val count2: Int = 0) : MvRxState
    @Test fun setSaveOneInt() {
        class TestViewModel(override val initialState: SetSaveOneIntState) : TestMvRxViewModel<SetSaveOneIntState>()
        val originalState = SetSaveOneIntState(count1 = 7, count2 = 9)
        val viewModel = TestViewModel(originalState)
        val newState = testViewModel(viewModel)
        assertEquals(originalState.count1, newState.count1)
        assertEquals(0, newState.count2)
    }

    data class InvalidViewModelState(@PersistState val count1: Int = 0, val count2: Int) : MvRxState
    @Test(expected = IllegalStateException::class) fun invalidViewModel() {
        class TestViewModel(override val initialState: InvalidViewModelState) : TestMvRxViewModel<InvalidViewModelState>()
        val originalState = InvalidViewModelState(count1 = 7, count2 = 9)
        val viewModel = TestViewModel(originalState)
        testViewModel(viewModel)
    }

    data class Args(val initialCount: Int)
    data class ArgState(val count1: Int = 0, val count2: Int) : MvRxState {
        constructor(args: Args) : this(count2 = args.initialCount)
    }
    @Test fun paramRestoredViaArgs() {
        class TestViewModel(override val initialState: ArgState) : TestMvRxViewModel<ArgState>()
        val originalState = ArgState(count1 = 7, count2 = 9)
        val viewModel = TestViewModel(originalState)
        val newState = testViewModel(viewModel, Args(1))
        assertEquals(1, newState.count2)
        assertEquals(0, newState.count1)
    }

    data class ArgPersistState(val count1: Int = 0, @PersistState val count2: Int) : MvRxState {
        constructor(args: Args) : this(count2 = args.initialCount)
    }
    @Test fun paramRestoredViaArgsOverwrittenWithState() {
        class TestViewModel(override val initialState: ArgPersistState) : TestMvRxViewModel<ArgPersistState>()
        val originalState = ArgPersistState(count1 = 7, count2 = 9)
        val viewModel = TestViewModel(originalState)
        val newState = testViewModel(viewModel, Args(1))
        assertEquals(9, newState.count2)
        assertEquals(0, newState.count1)
    }

    private fun <VM : TestMvRxViewModel<S>, S : MvRxState> testViewModel(viewModel: VM, args: Any? = null): S {
        val map = mutableMapOf("vm" to viewModel)
        val bundle = Bundle()
        store.saveViewModels(map, bundle)
        val outMap = mutableMapOf<String, ViewModel>()
        val newStore = MvRxViewModelStore(ViewModelStore())
        newStore.restoreViewModels(outMap, activity, bundle, args)
        @Suppress("UNCHECKED_CAST")
        val restoredViewModel: VM = outMap.getValue("vm") as VM
        assertNotEquals(viewModel, restoredViewModel)
        return withState(restoredViewModel) { it }
    }
}