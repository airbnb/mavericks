package com.airbnb.mvrx.todomvrx.data.source.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.airbnb.mvrx.todomvrx.data.Task

/**
 * The Room Database that contains the Task table.
 */
@Database(entities = [Task::class], version = 2)
abstract class ToDoDatabase : RoomDatabase() {

    abstract fun taskDao(): TasksDao

    companion object {

        private var INSTANCE: ToDoDatabase? = null

        private val lock = Any()

        fun getInstance(context: Context): ToDoDatabase {
            synchronized(lock) {
                if (INSTANCE == null) {
                    INSTANCE = Room
                        .databaseBuilder(context.applicationContext, ToDoDatabase::class.java, "Tasks.db")
                        .build()
                }
                return INSTANCE!!
            }
        }
    }
}
