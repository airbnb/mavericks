/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.airbnb.mvrx.todomvrx.todoapp.data.source.remote

import com.airbnb.mvrx.todomvrx.todoapp.data.Task
import com.airbnb.mvrx.todomvrx.todoapp.data.source.TasksDataSource
import com.airbnb.mvrx.todomvrx.todoapp.util.Optional
import io.reactivex.Single
import java.util.LinkedHashMap
import java.util.concurrent.TimeUnit

/**
 * Implementation of the data source that adds a latency simulating network.
 */
private const val SERVICE_LATENCY_IN_MILLIS = 2000
object TasksRemoteDataSource : TasksDataSource {

    private val TASKS_SERVICE_DATA: MutableMap<String, Task> = LinkedHashMap(2)

    init {
        arrayOf(
                Task("Build tower in Pisa", "Ground looks good, no foundation work required."),
                Task("Finish bridge in Tacoma", "Found awesome girders at half the cost!")
        ).forEach {
            TASKS_SERVICE_DATA[it.id] = it
        }
    }

    override fun getTasks(): Single<List<Task>> {
        return Single
                .just(TASKS_SERVICE_DATA.values.toList())
                .delay(SERVICE_LATENCY_IN_MILLIS.toLong(), TimeUnit.MILLISECONDS)
    }

    override fun getTask(taskId: String): Single<Optional<Task>> {
        val task = TASKS_SERVICE_DATA[taskId]
        return if (task != null) {
            Single.just(Optional(task)).delay(SERVICE_LATENCY_IN_MILLIS.toLong(), TimeUnit.MILLISECONDS)
        } else {
            Single.just(Optional<Task>(null))
        }
    }

    override fun saveTask(task: Task) {
        TASKS_SERVICE_DATA[task.id] = task
    }

    override fun completeTask(task: Task) {
        val completedTask = Task(task.title, task.description, task.id, true)
        TASKS_SERVICE_DATA[task.id] = completedTask
    }

    override fun completeTask(taskId: String) {
        // Not required for the remote data source because the {@link TasksRepository} handles
        // converting from a {@code taskId} to a {@link task} using its cached data.
    }

    override fun activateTask(task: Task) {
        val activeTask = Task(task.title, task.description, task.id)
        TASKS_SERVICE_DATA[task.id] = activeTask
    }

    override fun activateTask(taskId: String) {
        // Not required for the remote data source because the {@link TasksRepository} handles
        // converting from a {@code taskId} to a {@link task} using its cached data.
    }

    override fun clearCompletedTasks() {
        val it = TASKS_SERVICE_DATA.entries.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            if (entry.value.isCompleted) {
                it.remove()
            }
        }
    }

    override fun refreshTasks() {
        // Not required because the {@link TasksRepository} handles the logic of refreshing the
        // tasks from all the available data sources.
    }

    override fun deleteAllTasks() {
        TASKS_SERVICE_DATA.clear()
    }

    override fun deleteTask(taskId: String) {
        TASKS_SERVICE_DATA.remove(taskId)
    }
}
