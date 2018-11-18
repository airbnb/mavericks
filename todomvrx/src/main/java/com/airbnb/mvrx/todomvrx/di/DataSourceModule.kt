package com.airbnb.mvrx.todomvrx.di

import com.airbnb.mvrx.todomvrx.data.source.TasksDataSource
import com.airbnb.mvrx.todomvrx.data.source.db.DatabaseDataSource
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

/**
 * Bind [DatabaseDataSource] with [TasksDataSource]
 */
@Module
abstract class DataSourceModule {
    @Singleton
    @Binds
    abstract fun bindTaskDataSource(databaseDataSource: DatabaseDataSource): TasksDataSource
}