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
package com.airbnb.mvrx.todomvrx

import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.navigation.fragment.findNavController
import com.airbnb.epoxy.EpoxyController
import com.airbnb.mvrx.args
import com.airbnb.mvrx.todomvrx.core.BaseFragment
import com.airbnb.mvrx.todomvrx.data.findTask
import com.airbnb.mvrx.todomvrx.todoapp.R
import com.airbnb.mvrx.todomvrx.views.taskDetailView
import com.airbnb.mvrx.withState
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TaskDetailArgs(val id: String) : Parcelable

/**
 * Main UI for the task detail screen.
 */
class TaskDetailFragment : BaseFragment() {

    private val args: TaskDetailArgs by args()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        fab.setImageResource(R.drawable.ic_edit)
        fab.setOnClickListener {
            navigate(R.id.addEditFragment, AddEditTaskArgs(args.id))
        }
    }

    override fun EpoxyController.buildModels() = withState(viewModel) { state ->
        taskDetailView {
            id("detail")
            task(state.tasks.findTask(args.id))
            onCompleteChanged { viewModel.setComplete(args.id, it) }
        }
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
