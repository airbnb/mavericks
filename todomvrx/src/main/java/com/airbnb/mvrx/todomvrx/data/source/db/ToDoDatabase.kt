package com.airbnb.mvrx.todomvrx.data.source.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.airbnb.mvrx.todomvrx.data.Task

/**
 * The Room Database that contains the Task table.
 */
@Database(entities = [Task::class], version = 2)
abstract class ToDoDatabase : RoomDatabase() {
    abstract fun taskDao(): TasksDao
}