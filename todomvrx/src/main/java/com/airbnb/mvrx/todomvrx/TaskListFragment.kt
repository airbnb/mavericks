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

package com.airbnb.mvrx.todomvrx

import android.os.Bundle
import android.support.v7.widget.PopupMenu
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.airbnb.epoxy.EpoxyController
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.todomvrx.core.BaseFragment
import com.airbnb.mvrx.todomvrx.core.MvRxViewModel
import com.airbnb.mvrx.todomvrx.data.Task
import com.airbnb.mvrx.todomvrx.todoapp.R
import com.airbnb.mvrx.todomvrx.views.fullScreenMessageView
import com.airbnb.mvrx.todomvrx.views.header
import com.airbnb.mvrx.todomvrx.views.horizontalLoader
import com.airbnb.mvrx.todomvrx.views.taskItemView
import com.airbnb.mvrx.withState

data class TaskListState(val filter: TaskListFilter = TaskListFilter.All) : MvRxState

class TaskListViewModel(override val initialState: TaskListState) : MvRxViewModel<TaskListState>() {
    fun setFilter(filter: TaskListFilter) = setState { copy(filter = filter) }
}

/**
 * Display a grid of [Task]s. User can choose to view all, active or complete tasks.
 */
class TaskListFragment : BaseFragment() {

    private val taskListViewModel by fragmentViewModel(TaskListViewModel::class)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        fab.setImageResource(R.drawable.ic_add)

        fab.setOnClickListener {
            navigate(R.id.addEditFragment, AddEditTaskArgs())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.tasks_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?) = when (item?.itemId ?: 0) {
        R.id.menu_refresh -> viewModel.refreshTasks().andTrue()
        R.id.menu_filter -> showFilteringPopUpMenu().andTrue()
        R.id.menu_clear -> viewModel.clearCompletedTasks().andTrue()
        else -> super.onOptionsItemSelected(item)
    }

    override fun EpoxyController.buildModels() = withState(viewModel, taskListViewModel) { state, taskListState ->
        // We always want to show this so the content won't snap up when the loader finishes.
        horizontalLoader {
            id("loader")
            loading(state.isLoading)
        }

        if (state.tasks.isEmpty() && !state.isLoading) {
            val (title, iconRes) = when (taskListState.filter) {
                TaskListFilter.All -> R.string.no_tasks_all to R.drawable.ic_assignment_turned_in_24dp
                TaskListFilter.Active -> R.string.no_tasks_active to R.drawable.ic_check_circle_24dp
                TaskListFilter.Completed -> R.string.no_tasks_completed to R.drawable.ic_verified_user_24dp
            }
            fullScreenMessageView {
                id("empty_message")
                iconRes(iconRes)
                title(title)
            }
        } else if (!(state.isLoading && state.tasks.isEmpty())) {
            header {
                id("header")
                title(when (taskListState.filter) {
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
                            title(task.title)
                            checked(task.complete)
                            onCheckedChanged { completed -> viewModel.setComplete(task.id, completed) }
                            onClickListener { _ -> navigate(R.id.detailFragment, TaskDetailArgs(task.id)) }
                        }
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
