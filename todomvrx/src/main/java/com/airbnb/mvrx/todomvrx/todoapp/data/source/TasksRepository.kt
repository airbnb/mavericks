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

package com.airbnb.mvrx.todomvrx.todoapp.data.source

import android.support.annotation.VisibleForTesting
import com.airbnb.mvrx.todomvrx.todoapp.data.Task
import com.airbnb.mvrx.todomvrx.todoapp.util.Optional
import io.reactivex.Single
import java.util.LinkedHashMap

/**
 * Concrete implementation to load tasks from the data sources into a cache.
 *
 *
 * For simplicity, this implements a dumb synchronisation between locally persisted data and data
 * obtained from the server, by using the remote data source only if the local database doesn't
 * exist or is empty.
 */
class TasksRepository(
        private val tasksRemoteDataSource: TasksDataSource,
        private val tasksLocalDataSource: TasksDataSource
) : TasksDataSource {

    /**
     * This variable has package local visibility so it can be accessed from tests.
     */
    @VisibleForTesting
    internal val cache: MutableMap<String, Task> = LinkedHashMap()

    /**
     * Marks the cache as invalid, to force an update the next time data is requested. This variable
     * has package local visibility so it can be accessed from tests.
     */
    @VisibleForTesting
    internal var isCacheDirty = true

    private val getAndCacheLocalTasks: Single<List<Task>>
        get() = tasksLocalDataSource.tasks
                .flatMap { tasks ->
                    tasks.forEach { cache[it.id] = it }

                    Single.just(tasks)
                }

    private val getAndSaveRemoteTasks: Single<List<Task>>
        get() = tasksRemoteDataSource.tasks
                .flatMap { tasks ->
                    tasks.forEach { cache[it.id] = it }
                    isCacheDirty = false
                    Single.just(tasks)
                }

    /**
     * Gets tasks from cache, local data source (SQLite) or remote data source, whichever is
     * available first.
     */
    override fun getTasks(): Single<List<Task>> {
        // Respond immediately with cache if available and not dirty
        if (!isCacheDirty) {
            return Single.just(cache.values.toList())
        }

        return Single.concat(getAndSaveRemoteTasks, getAndCacheLocalTasks)
                .filter { tasks -> !tasks.isEmpty() }
                .firstOrError()
    }

    override fun saveTask(task: Task) {
        tasksRemoteDataSource.saveTask(task)
        tasksLocalDataSource.saveTask(task)

        cache[task.id] = task
    }

    override fun completeTask(task: Task) {
        tasksRemoteDataSource.completeTask(task)
        tasksLocalDataSource.completeTask(task)

        val completedTask = Task(task.title, task.description, task.id, true)

        cache[task.id] = completedTask
    }

    override fun completeTask(taskId: String) {
        cache[taskId]?.let { completeTask(it) }
    }

    override fun activateTask(task: Task) {
        tasksRemoteDataSource.activateTask(task)
        tasksLocalDataSource.activateTask(task)

        val activeTask = Task(task.title, task.description, task.id)

        cache[task.id] = activeTask
    }

    override fun activateTask(taskId: String) {
        cache[taskId]?.let { activateTask(it) }
    }

    override fun clearCompletedTasks() {
        tasksRemoteDataSource.clearCompletedTasks()
        tasksLocalDataSource.clearCompletedTasks()

        val it = cache.entries.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            if (entry.value.isCompleted) {
                it.remove()
            }
        }
    }

    /**
     * Gets tasks from local data source (sqlite) unless the table is new or empty. In that case it
     * uses the network data source. This is done to simplify the sample.
     */
    override fun getTask(taskId: String): Single<Optional<Task>> {
        cache[taskId]?.let {
            return Single.just(Optional(it))
        }

        // Is the task in the local data source? If not, query the network.
        val localTask = getTaskWithIdFromLocalRepository(taskId)
        val remoteTask = tasksRemoteDataSource
                .getTask(taskId)
                .doAfterSuccess {
                    it()?.let { task -> cache[task.id] = task }
                }

        return Single.concat<Optional<Task>>(localTask, remoteTask).firstOrError()
    }

    override fun refreshTasks() {
        isCacheDirty = true
    }

    override fun deleteAllTasks() {
        tasksRemoteDataSource.deleteAllTasks()
        tasksLocalDataSource.deleteAllTasks()

        cache.clear()
    }

    override fun deleteTask(taskId: String) {
        tasksRemoteDataSource.deleteTask(taskId)
        tasksLocalDataSource.deleteTask(taskId)

        cache.remove(taskId)
    }

    internal fun getTaskWithIdFromLocalRepository(taskId: String): Single<Optional<Task>> {
        return tasksLocalDataSource
                .getTask(taskId)
                .doAfterSuccess {
                    it()?.let { task -> cache[task.id] = task }
                }
    }

    companion object {

        private var INSTANCE: TasksRepository? = null

        /**
         * Returns the single instance of this class, creating it if necessary.
         *
         * @param tasksRemoteDataSource the backend data source
         * @param tasksLocalDataSource  the device storage data source
         * @return the [TasksRepository] instance
         */
        fun getInstance(tasksRemoteDataSource: TasksDataSource, tasksLocalDataSource: TasksDataSource): TasksRepository {
            if (INSTANCE == null) {
                INSTANCE = TasksRepository(tasksRemoteDataSource, tasksLocalDataSource)
            }
            return INSTANCE!!
        }

        /**
         * Used to force [.getInstance] to create a new instance
         * next time it's called.
         */
        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
