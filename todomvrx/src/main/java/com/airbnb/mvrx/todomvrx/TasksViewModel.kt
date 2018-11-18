package com.airbnb.mvrx.todomvrx

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.todomvrx.core.MvRxViewModel
import com.airbnb.mvrx.todomvrx.data.Task
import com.airbnb.mvrx.todomvrx.data.Tasks
import com.airbnb.mvrx.todomvrx.data.findTask
import com.airbnb.mvrx.todomvrx.data.source.TasksDataSource
import com.airbnb.mvrx.todomvrx.util.copy
import com.airbnb.mvrx.todomvrx.util.delete
import com.airbnb.mvrx.todomvrx.util.upsert
import javax.inject.Inject

data class TasksState(
        val tasks: Tasks = emptyList(),
        val taskRequest: Async<Tasks> = Uninitialized,
        val isLoading: Boolean = false,
        val lastEditedTask: String? = null
) : MvRxState

class TasksViewModel @Inject constructor(private val source: TasksDataSource) :
        MvRxViewModel<TasksState>(TasksState()) {

    init {
        refreshTasks()
    }

    fun refreshTasks() {
        source.getTasks().toObservable()
                .doOnSubscribe { setState { copy(isLoading = true) } }
                .doOnComplete { setState { copy(isLoading = false) } }
                .execute { copy(taskRequest = it, tasks = it() ?: tasks, lastEditedTask = null) }
    }

    fun upsertTask(task: Task) {
        setState { copy(tasks = tasks.upsert(task) { it.id == task.id }, lastEditedTask =  task.id) }
        source.upsertTask(task)
    }

    fun setComplete(id: String, complete: Boolean) {
        setState {
            val task = tasks.findTask(id) ?: return@setState this
            if (task.complete == complete) return@setState this
            copy(tasks = tasks.copy(tasks.indexOf(task), task.copy(complete = complete)), lastEditedTask = id)

        }
        source.setComplete(id, complete)
    }

    fun clearCompletedTasks() = setState {
        source.clearCompletedTasks()
        copy(tasks = tasks.filter { !it.complete }, lastEditedTask = null)
    }

    fun deleteTask(id: String) {
        setState { copy(tasks = tasks.delete { it.id == id }, lastEditedTask = id) }
        source.deleteTask(id)
    }
}

