/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.airbnb.mvrx.todomvrx.data.source.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.airbnb.mvrx.todomvrx.data.Task
import com.airbnb.mvrx.todomvrx.data.Tasks
import io.reactivex.Single

/**
 * Data Access Object for the tasks table.
 */
@Dao interface TasksDao {

    /**
     * Select all tasks from the tasks table.
     *
     * @return all tasks.
     */
    @Query("SELECT * FROM Tasks")
    fun getTasks(): Single<Tasks>

    /**
     * Insert a task in the database. If the task already exists, replace it.
     *
     * @param task the task to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveTask(task: Task): Long

    /**
     * Update the complete status of a task
     *
     * @param id    id of the task
     * @param complete status to be updated
     */
    @Query("UPDATE tasks SET complete = :complete WHERE id = :id")
    fun setComplete(id: String, complete: Boolean): Long

    /**
     * Delete a task by id.
     */
    @Query("DELETE FROM Tasks WHERE id = :id")
    fun deleteTask(id: String): Int

    /**
     * Delete all complete tasks from the table.
     *
     * @return the number of tasks deleted.
     */
    @Query("DELETE FROM Tasks WHERE complete = 1")
    fun clearCompletedTasks(): Int
}