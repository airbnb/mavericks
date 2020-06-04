package com.airbnb.mvrx.todomvrx.data

typealias Tasks = List<Task>

fun Tasks.findTask(id: String?) = firstOrNull { it.id == id }
