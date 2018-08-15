package com.airbnb.mvrx.todomvrx.todoapp.tasks

import com.airbnb.mvrx.todomvrx.todoapp.data.Task

enum class TaskListFilter {
    All,
    Active,
    Completed;

    fun matches(task: Task): Boolean = if (task.complete) this != Completed else this != Completed
}