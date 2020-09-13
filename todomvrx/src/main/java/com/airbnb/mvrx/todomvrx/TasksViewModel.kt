package com.airbnb.mvrx.todomvrx

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.ViewModelContext
import com.airbnb.mvrx.todomvrx.core.MvRxViewModel
import com.airbnb.mvrx.todomvrx.data.Task
import com.airbnb.mvrx.todomvrx.data.Tasks
import com.airbnb.mvrx.todomvrx.data.findTask
import com.airbnb.mvrx.todomvrx.data.source.TasksDataSource
import com.airbnb.mvrx.todomvrx.data.source.db.DatabaseDataSource
import com.airbnb.mvrx.todomvrx.data.source.db.ToDoDatabase
import com.airbnb.mvrx.todomvrx.util.copy
import com.airbnb.mvrx.todomvrx.util.delete
import com.airbnb.mvrx.todomvrx.util.upsert
import io.reactivex.Observable

data class TasksState(
    val tasks: Tasks = emptyList(),
    val taskRequest: Async<Tasks> = Uninitialized,
    val isLoading: Boolean = false,
    val lastEditedTask: String? = null
) : MavericksState

class TasksViewModel(initialState: TasksState, private val sources: List<TasksDataSource>) : MvRxViewModel<TasksState>(initialState) {

    init {
        logStateChanges()
        refreshTasks()
    }

    fun refreshTasks() {
        Observable.merge(sources.map { it.getTasks().toObservable() })
            .doOnSubscribe { setState { copy(isLoading = true) } }
            .doOnComplete { setState { copy(isLoading = false) } }
            .execute { copy(taskRequest = it, tasks = it() ?: tasks, lastEditedTask = null) }
    }

    fun upsertTask(task: Task) {
        setState { copy(tasks = tasks.upsert(task) { it.id == task.id }, lastEditedTask = task.id) }
        sources.forEach { it.upsertTask(task) }
    }

    fun setComplete(id: String, complete: Boolean) {
        setState {
            val task = tasks.findTask(id) ?: return@setState this
            if (task.complete == complete) return@setState this
            copy(tasks = tasks.copy(tasks.indexOf(task), task.copy(complete = complete)), lastEditedTask = id)
        }
        sources.forEach { it.setComplete(id, complete) }
    }

    fun clearCompletedTasks() = setState {
        sources.forEach { it.clearCompletedTasks() }
        copy(tasks = tasks.filter { !it.complete }, lastEditedTask = null)
    }

    fun deleteTask(id: String) {
        setState { copy(tasks = tasks.delete { it.id == id }, lastEditedTask = id) }
        sources.forEach { it.deleteTask(id) }
    }

    companion object : MavericksViewModelFactory<TasksViewModel, TasksState> {

        override fun create(viewModelContext: ViewModelContext, state: TasksState): TasksViewModel {
            val database = ToDoDatabase.getInstance(viewModelContext.activity)
            // Simulate data sources of different speeds.
            // The slower one can be thought of as the network data source.
            val dataSource1 = DatabaseDataSource(database.taskDao(), 2000)
            val dataSource2 = DatabaseDataSource(database.taskDao(), 3500)
            return TasksViewModel(state, listOf(dataSource1, dataSource2))
        }
    }
}
