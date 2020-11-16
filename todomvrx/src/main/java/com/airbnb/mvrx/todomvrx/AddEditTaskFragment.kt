/*
 * Copyright 2017, The Android Open Source Project
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

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.navigation.fragment.findNavController
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.args
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.todomvrx.core.BaseFragment
import com.airbnb.mvrx.todomvrx.core.MvRxViewModel
import com.airbnb.mvrx.todomvrx.data.Task
import com.airbnb.mvrx.todomvrx.data.findTask
import com.airbnb.mvrx.todomvrx.todoapp.R
import com.airbnb.mvrx.todomvrx.util.simpleController
import com.airbnb.mvrx.todomvrx.views.addEditView
import com.airbnb.mvrx.withState
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.parcel.Parcelize

@SuppressLint("ParcelCreator")
@Parcelize
data class AddEditTaskArgs(val id: String? = null) : Parcelable

data class EditTaskState(val newTitle: String? = null, val newDescription: String? = null) : MavericksState
class EditTaskViewModel(initialState: EditTaskState) : MvRxViewModel<EditTaskState>(initialState) {
    fun setTitle(title: String) {
        setState { copy(newTitle = title) }
    }

    fun setDescription(description: String) {
        setState { copy(newDescription = description) }
    }
}

/**
 * Main UI for the add task screen. Users can enter a task title and description.
 */
class AddEditTaskFragment : BaseFragment() {
    private val taskViewModel: EditTaskViewModel by fragmentViewModel()

    private val args: AddEditTaskArgs by args()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize fragment state if we are editing an existing task
        withState(taskViewModel, viewModel) { thisTaskState, allTaskState ->
            val task = allTaskState.tasks.findTask(args.id)

            if (thisTaskState.newTitle == null) {
                taskViewModel.setTitle(task?.title.orEmpty())
            }

            if (thisTaskState.newDescription == null) {
                taskViewModel.setDescription(task?.description.orEmpty())
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fab.setImageResource(R.drawable.ic_done)
        fab.setOnClickListener {
            withState(taskViewModel, viewModel) { thisTaskState, allTaskState ->
                val newTitle = thisTaskState.newTitle
                val newDescription = thisTaskState.newDescription

                if (newTitle.isNullOrBlank()) {
                    Snackbar.make(coordinatorLayout, R.string.empty_task_message, Snackbar.LENGTH_LONG).show()
                    return@withState
                }

                val task = (allTaskState.tasks.findTask(args.id) ?: Task()).run {
                    copy(title = newTitle, description = newDescription ?: description)
                }
                viewModel.upsertTask(task)
                findNavController().navigateUp()
            }
        }
    }

    override fun epoxyController() = simpleController(taskViewModel) { state ->
        addEditView {
            id("add_edit")
            title(state.newTitle)
            description(state.newDescription)
            onTitleChanged { taskViewModel.setTitle(it) }
            onDescriptionChanged { taskViewModel.setDescription(it) }
        }
    }

    override fun onDestroyView() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
        super.onDestroyView()
    }
}
