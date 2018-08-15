package com.airbnb.mvrx.todomvrx.data.source

import com.airbnb.mvrx.todomvrx.data.Task
import com.airbnb.mvrx.todomvrx.data.Tasks
import io.reactivex.Single
import io.reactivex.disposables.Disposable

interface TasksDataSource {
    fun getTasks(): Single<Tasks>

    fun upsertTask(task: Task): Disposable

    fun setComplete(id: String, complete: Boolean): Disposable

    fun clearCompletedTasks(): Disposable

    fun deleteTask(id: String): Disposable
}
