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

package com.airbnb.mvrx.todomvrx.todoapp.tasks

import android.os.Bundle
import android.support.v7.widget.PopupMenu
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.airbnb.epoxy.EpoxyController
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.todomvrx.todoapp.R
import com.airbnb.mvrx.todomvrx.todoapp.TasksViewModel
import com.airbnb.mvrx.todomvrx.todoapp.core.BaseFragment
import com.airbnb.mvrx.todomvrx.todoapp.data.Task
import com.airbnb.mvrx.todomvrx.todoapp.data.source.db.TasksLocalDataSource
import com.airbnb.mvrx.todomvrx.todoapp.data.source.db.ToDoDatabase
import com.airbnb.mvrx.todomvrx.todoapp.util.AppExecutors
import com.airbnb.mvrx.todomvrx.todoapp.views.header
import com.airbnb.mvrx.todomvrx.todoapp.views.horizontalLoader
import com.airbnb.mvrx.todomvrx.todoapp.views.taskItemView
import com.airbnb.mvrx.withState

/**
 * Display a grid of [Task]s. User can choose to view all, active or complete tasks.
 */
class TaskListFragment : BaseFragment() {

    private val tasksViewModel by activityViewModel(TasksViewModel::class)
    private val taskListViewModel by fragmentViewModel(TaskListViewModel::class)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        fab.setImageResource(R.drawable.ic_add)

        val database = ToDoDatabase.getInstance(requireContext())
        TasksLocalDataSource.getInstance(AppExecutors(), database.taskDao())

        fab.setOnClickListener { TODO() }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.tasks_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?) = when (item?.itemId ?: 0) {
        R.id.menu_refresh -> tasksViewModel.refreshTasks().andTrue()
        R.id.menu_filter -> showFilteringPopUpMenu().andTrue()
        R.id.menu_clear -> tasksViewModel.clearCompletedTasks().andTrue()
        else -> super.onOptionsItemSelected(item)
    }

    override fun EpoxyController.buildModels() = withState(tasksViewModel, taskListViewModel) { state, taskListState ->

        horizontalLoader {
            id("loader")
            loading(state.isLoading)
        }

        header {
            id("header")
            title(when(taskListState.filter) {
                TaskListFilter.All -> R.string.label_all
                TaskListFilter.Active -> R.string.label_active
                TaskListFilter.Completed -> R.string.label_completed
            })
        }

        state.tasks
                .filter(taskListState.filter::matches)
                .forEach { task ->
                    taskItemView {
                        id(task.id)
                        title(task.displayTitle)
                        checked(task.complete)
                        onCheckedChanged { completed -> tasksViewModel.setComplete(task.id, completed) }
                    }
                }
    }

    private fun showFilteringPopUpMenu() {
        PopupMenu(requireContext(), requireActivity().findViewById<View>(R.id.menu_filter)).run {
            menuInflater.inflate(R.menu.filter_tasks, menu)

            setOnMenuItemClickListener {
                val filter = when (it.itemId) {
                    R.id.active -> TaskListFilter.Active
                    R.id.completed -> TaskListFilter.Completed
                    R.id.all -> TaskListFilter.All
                    else -> TaskListFilter.All
                }
                taskListViewModel.setFilter(filter)
                true
            }
            show()
        }
    }

    @Suppress("unused")
    private fun Unit.andTrue() = true

    companion object {
        fun newInstance() = TaskListFragment()
    }
}
