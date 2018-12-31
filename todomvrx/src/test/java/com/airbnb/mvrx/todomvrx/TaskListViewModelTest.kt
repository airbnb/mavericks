package com.airbnb.mvrx.todomvrx

import com.airbnb.mvrx.test.MvRxTestRule
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test

class TaskListViewModelTest {
    private lateinit var viewModel: TaskListViewModel

    @Before
    fun setup() {
        viewModel = TaskListViewModel(TaskListState())
    }

    @Test
    fun testCanChangeTaskListFilter() {
        viewModel.setFilter(TaskListFilter.All)
        assertEquals(mvrxTestRule.getState(viewModel).filter, TaskListFilter.All)
        viewModel.setFilter(TaskListFilter.Active)
        assertEquals(mvrxTestRule.getState(viewModel).filter, TaskListFilter.Active)
    }

    companion object {
        @JvmField
        @ClassRule
        val mvrxTestRule = MvRxTestRule()
    }
}