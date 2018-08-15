package com.airbnb.mvrx.todomvrx.data.source.db

import com.airbnb.mvrx.todomvrx.data.Task
import com.airbnb.mvrx.todomvrx.data.source.TasksDataSource
import io.reactivex.Completable

class DatabaseDataSource(private val dao: TasksDao) : TasksDataSource {
    override fun getTasks() = dao.getTasks()

    override fun saveTask(task: Task) = Completable.fromCallable { dao.saveTask(task) }

    override fun setComplete(id: String, complete: Boolean) = Completable.fromCallable { dao.setComplete(id, complete) }

    override fun clearCompletedTasks() = Completable.fromCallable { dao.clearCompletedTasks() }

    override fun deleteTask(id: String) = Completable.fromCallable { dao.deleteTask(id) }
\