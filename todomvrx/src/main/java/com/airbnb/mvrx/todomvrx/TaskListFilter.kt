package com.airbnb.mvrx.todomvrx

import com.airbnb.mvrx.todomvrx.data.Task

enum class TaskListFilter {
    All,
    Active,
    Completed;

    fun matches(task: Task): Boolean = if (task.complete) this != Active else this != Completed
}
