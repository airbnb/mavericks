package com.airbnb.mvrx.todomvrx.todoapp.data

typealias Tasks = List<Task>
fun Tasks.findTask(id: String) = firstOrNull { it.id == id }