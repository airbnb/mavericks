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

package com.airbnb.mvrx.todomvrx.data.source.remote

import com.airbnb.mvrx.todomvrx.data.Task
import com.airbnb.mvrx.todomvrx.data.Tasks
import com.airbnb.mvrx.todomvrx.data.source.TasksDataSource
import io.reactivex.Completable
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
//                Task("Build tower in Pisa", "Ground looks good, no foundation work required."),
//                Task("Finish bridge in Tacoma", "Found awesome girders at half the cost!")
                Task("A", "a")
        ).forEach {
            TASKS_SERVICE_DATA[it.id] = it
        }
    }

    override fun getTasks(): Single<Tasks> {
        return Single
                .just(TASKS_SERVICE_DATA.values.toList())
                .delay(SERVICE_LATENCY_IN_MILLIS.toLong(), TimeUnit.MILLISECONDS)
    }

    override fun setComplete(id: String, complete: Boolean): Completable = Completable.fromCallable {
        TASKS_SERVICE_DATA[id]?.let { remoteTask ->
            remoteTask.copy(complete = complete).also {
                TASKS_SERVICE_DATA[remoteTask.id] = it
            }
        } ?: throw IllegalStateException("Task $id not found")
    }

    override fun saveTask(task: Task) = Completable.fromCallable {
        TASKS_SERVICE_DATA[task.id] = task
        task
    }

    override fun clearCompletedTasks() = Completable.fromCallable {
        val it = TASKS_SERVICE_DATA.entries.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            if (entry.value.complete) {
                it.remove()
            }
        }
    }

    override fun deleteTask(taskId: String) = Completable.fromCallable {
        TASKS_SERVICE_DATA.remove(taskId)
    }
}
