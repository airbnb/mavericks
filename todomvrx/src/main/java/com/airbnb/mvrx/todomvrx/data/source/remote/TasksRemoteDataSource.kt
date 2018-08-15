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
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.LinkedHashMap
import java.util.concurrent.TimeUnit

/**
 * Implementation of the data source that adds a latency simulating network.
 */
class TasksRemoteDataSource(
        private val latencyMs: Long = 2000,
        private val scheduler: Scheduler
) : TasksDataSource {

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
                .delay(latencyMs, TimeUnit.MILLISECONDS)
    }

    override fun setComplete(id: String, complete: Boolean): Disposable = fromAction {
        TASKS_SERVICE_DATA[id]?.let { remoteTask ->
            remoteTask.copy(complete = complete).also {
                TASKS_SERVICE_DATA[remoteTask.id] = it
            }
        } ?: throw IllegalStateException("Task $id not found")
    }

    override fun saveTask(task: Task): Disposable = fromAction {
        TASKS_SERVICE_DATA[task.id] = task
    }

    override fun clearCompletedTasks(): Disposable = fromAction {
        val it = TASKS_SERVICE_DATA.entries.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            if (entry.value.complete) {
                it.remove()
            }
        }
    }

    override fun deleteTask(id: String): Disposable = fromAction {
        TASKS_SERVICE_DATA.remove(id)
    }

    private fun fromAction(action: () -> Unit): Disposable = Completable.fromAction(action)
            .subscribeOn(scheduler)
            .observeOn(AndroidSchedulers.mainThread())
            .delay(latencyMs, TimeUnit.MILLISECONDS)
            .subscribe()
}
