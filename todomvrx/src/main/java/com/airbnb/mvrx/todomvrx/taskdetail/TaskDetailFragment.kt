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
package com.airbnb.mvrx.todomvrx.taskdetail

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.args
import com.airbnb.mvrx.todomvrx.TasksViewModel
import com.airbnb.mvrx.todomvrx.addedittask.AddEditTaskArgs
import com.airbnb.mvrx.todomvrx.core.BaseFragment
import com.airbnb.mvrx.todomvrx.data.findTask
import com.airbnb.mvrx.todomvrx.todoapp.R
import com.airbnb.mvrx.withState
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TaskDetailArgs(val id: String) : Parcelable

/**
 * Main UI for the task detail screen.
 */
class TaskDetailFragment : BaseFragment() {

    private val viewModel by activityViewModel(TasksViewModel::class)
    private val args: TaskDetailArgs by args()

    private lateinit var checkbox: CheckBox
    private lateinit var titleView: TextView
    private lateinit var descriptionView: TextView
    private lateinit var noDataView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.frag_detail, container, false).apply {
                fab = findViewById(R.id.fab)
                titleView = findViewById(R.id.task_title)
                descriptionView = findViewById(R.id.task_description)
                checkbox = findViewById(R.id.complete)
                noDataView = findViewById(R.id.no_data_view)
            }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        fab.setImageResource(R.drawable.ic_edit)
        fab.setOnClickListener {
            navigate(R.id.addEditFragment, AddEditTaskArgs(args.id))
        }
        checkbox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setComplete(args.id, isChecked)

        }
        // Must be called to trigger the lazy delegate
        viewModel
    }

    override fun invalidate() = withState(viewModel) { state ->
        val task = state.tasks.findTask(args.id)
        checkbox.isChecked = task?.complete ?: false
        checkbox.visibility = if (task == null) View.GONE else View.VISIBLE
        titleView.text = task?.title
        descriptionView.text = task?.description
        noDataView.visibility = if (task == null) View.VISIBLE else View.GONE
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menu_delete -> {
            viewModel.deleteTask(args.id)
            findNavController().navigateUp()
            true
        }
        else -> false
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.taskdetail_fragment_menu, menu)
    }
}
