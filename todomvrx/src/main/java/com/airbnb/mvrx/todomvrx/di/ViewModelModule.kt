package com.airbnb.mvrx.todomvrx.di

import android.arch.lifecycle.ViewModel
import com.airbnb.mvrx.todomvrx.TaskListViewModel
import com.airbnb.mvrx.todomvrx.TasksViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Provide all ViewModels to [ViewModelFactory]
 */
@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(TasksViewModel::class)
    abstract fun bindViewModel1(viewModel: TasksViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TaskListViewModel::class)
    abstract fun bindViewModel2(viewModel: TaskListViewModel): ViewModel

}