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

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.support.design.widget.Snackbar
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.navigation.fragment.findNavController
import com.airbnb.epoxy.EpoxyController
import com.airbnb.mvrx.args
import com.airbnb.mvrx.todomvrx.core.BaseFragment
import com.airbnb.mvrx.todomvrx.data.Task
import com.airbnb.mvrx.todomvrx.data.findTask
import com.airbnb.mvrx.todomvrx.todoapp.R
import com.airbnb.mvrx.todomvrx.util.asSequence
import com.airbnb.mvrx.todomvrx.views.AddEditView
import com.airbnb.mvrx.todomvrx.views.addEditView
import com.airbnb.mvrx.withState
import kotlinx.android.parcel.Parcelize


@Parcelize
data class AddEditTaskArgs(val id: String? = null) : Parcelable

/**
 * Main UI for the add task screen. Users can enter a task title and description.
 */
class AddEditTaskFragment : BaseFragment() {

    private val args: AddEditTaskArgs by args()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fab.setImageResource(R.drawable.ic_done)
        fab.setOnClickListener {
            val (title, description) = recyclerView.asSequence().filterIsInstance(AddEditView::class.java).first().data()

            if (title.isEmpty()) {
                Snackbar.make(coordinatorLayout, R.string.empty_task_message, Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            withState(viewModel) { state ->
                val task = (state.tasks.findTask(args.id) ?: Task()).copy(title = title, description = description)
                viewModel.upsertTask(task)
                findNavController().navigateUp()
            }
        }
    }

    override fun EpoxyController.buildModels() = withState(viewModel) { state ->
        val task = state.tasks.findTask(args.id)
        addEditView {
            id("add_edit")
            title(task?.title)
            description(task?.description)
        }
    }

    override fun onDestroyView() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
        super.onDestroyView()
    }
}
