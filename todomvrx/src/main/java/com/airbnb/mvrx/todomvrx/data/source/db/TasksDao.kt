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