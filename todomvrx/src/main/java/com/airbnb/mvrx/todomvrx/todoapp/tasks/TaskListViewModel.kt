package com.airbnb.mvrx.todomvrx.todoapp.tasks

import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.todomvrx.todoapp.core.MvRxViewModel

data class TaskListState(val filter: TaskListFilter = TaskListFilter.All) : MvRxState

class TaskListViewModel(override val initialState: TaskListState) : MvRxViewModel<TaskListState>() {
    fun setFilter(filter: TaskListFilter) = setState { copy(filter = filter) }
}