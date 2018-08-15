package com.airbnb.mvrx.todomvrx.tasks

import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.todomvrx.core.MvRxViewModel

data class TaskListState(val filter: TaskListFilter = TaskListFilter.All) : MvRxState

class TaskListViewModel(override val initialState: TaskListState) : MvRxViewModel<TaskListState>() {
    fun setFilter(filter: TaskListFilter) = setState { copy(filter = filter) }
}