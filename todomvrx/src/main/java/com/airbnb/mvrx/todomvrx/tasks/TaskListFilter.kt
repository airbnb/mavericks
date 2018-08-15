package com.airbnb.mvrx.todomvrx.tasks

import com.airbnb.mvrx.todomvrx.data.Task

enum class TaskListFilter {
    All,
    Active,
    Completed;

    fun matches(task: Task): Boolean = if (task.complete) this != Completed else this != Completed
}