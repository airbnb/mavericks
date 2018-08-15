package com.airbnb.mvrx.todomvrx.todoapp

import android.support.v4.app.FragmentActivity
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.todomvrx.todoapp.core.MvRxViewModel
import com.airbnb.mvrx.todomvrx.todoapp.data.Task
import com.airbnb.mvrx.todomvrx.todoapp.data.source.TasksRepository
import com.airbnb.mvrx.todomvrx.todoapp.data.source.local.TasksLocalDataSource
import com.airbnb.mvrx.todomvrx.todoapp.data.source.local.ToDoDatabase
import com.airbnb.mvrx.todomvrx.todoapp.data.source.remote.TasksRemoteDataSource
import com.airbnb.mvrx.todomvrx.todoapp.util.AppExecutors

data class TasksState(
        val tasks: List<Task> = emptyList(),
        val tasksRequest: Async<List<Task>> = Uninitialized
) : MvRxState

class TasksViewModel(override val initialState: TasksState, private val repository: TasksRepository) : MvRxViewModel<TasksState>() {

    init {
        fetchTasks()
    }

    fun fetchTasks() {
        repository.tasks.execute { copy(tasks = it() ?: tasks, tasksRequest = it) }
    }

    companion object : MvRxViewModelFactory<TasksState> {
        override fun create(activity: FragmentActivity, state: TasksState): BaseMvRxViewModel<TasksState> {
            // TODO: make this more optimal
            val database = ToDoDatabase.getInstance(activity)
            val localDataSource = TasksLocalDataSource.getInstance(AppExecutors(), database.taskDao())
            val repository = TasksRepository.getInstance(localDataSource, TasksRemoteDataSource)
            return TasksViewModel(state, repository)
        }

    }
}

