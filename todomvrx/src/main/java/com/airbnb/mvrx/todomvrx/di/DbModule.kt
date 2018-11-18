package com.airbnb.mvrx.todomvrx.di

import android.app.Application
import android.arch.persistence.room.Room
import com.airbnb.mvrx.todomvrx.data.source.db.TasksDao
import com.airbnb.mvrx.todomvrx.data.source.db.ToDoDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [DataSourceModule::class])
class DbModule {

    @Singleton
    @Provides
    fun provideDatabase(application: Application): ToDoDatabase {
        return Room.databaseBuilder(application, ToDoDatabase::class.java, "Tasks.db").build()
    }

    @Singleton
    @Provides
    fun provideTasksDao(database: ToDoDatabase): TasksDao {
        return database.taskDao()
    }

}