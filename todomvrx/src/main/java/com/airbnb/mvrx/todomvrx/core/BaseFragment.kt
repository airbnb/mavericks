package com.airbnb.mvrx.todomvrx.core

import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.CallSuper
import android.support.annotation.IdRes
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyRecyclerView
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.MvRx
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.propertyWhitelist
import com.airbnb.mvrx.todomvrx.TasksState
import com.airbnb.mvrx.todomvrx.TasksViewModel
import com.airbnb.mvrx.todomvrx.data.findTask
import com.airbnb.mvrx.todomvrx.todoapp.R
import com.airbnb.mvrx.todomvrx.util.showLongSnackbar

abstract class BaseFragment : BaseMvRxFragment() {

    protected val viewModel by activityViewModel(TasksViewModel::class)

    protected lateinit var coordinatorLayout: CoordinatorLayout
    protected lateinit var recyclerView: EpoxyRecyclerView
    protected lateinit var fab: FloatingActionButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_base, container, false).apply {
                coordinatorLayout = findViewById(R.id.coordinator_layout)
                fab = findViewById(R.id.fab)
                recyclerView = findViewById(R.id.recycler_view)
                recyclerView.buildModelsWith { it.buildModels() }
            }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.subscribeWithHistory(propertyWhitelist(TasksState::tasks)) { oldState, newState ->
            if (oldState.tasks.any { it.complete } && !newState.tasks.any { it.complete }) {
                coordinatorLayout.showLongSnackbar(R.string.completed_tasks_cleared)
            }

            val oldTask = oldState.tasks.findTask(newState.lastEditedTask)
            val newTask = newState.tasks.findTask(newState.lastEditedTask)
            if (oldTask == newTask) return@subscribeWithHistory
            val message = when {
                oldTask == null -> R.string.successfully_added_task_message
                newTask == null -> R.string.successfully_deleted_task_message
                oldTask.title != newTask.title || oldTask.description != newTask.description ->
                    R.string.successfully_saved_task_message
                oldTask.complete && !newTask.complete -> R.string.task_marked_active
                !oldTask.complete && newTask.complete -> R.string.task_marked_complete
                else -> 0
            }
            if (message != 0) {
                coordinatorLayout.showLongSnackbar(message)
            }
        }

        viewModel.subscribe(propertyWhitelist(TasksState::isLoading)) { state ->
            if (state.taskRequest is Error) {
                coordinatorLayout.showLongSnackbar(R.string.loading_tasks_error)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // https://github.com/airbnb/MvRx/issues/15
        invalidate()
    }

    override fun invalidate() {
        recyclerView.requestModelBuild()
    }

    open fun EpoxyController.buildModels() {}

    protected fun navigate(@IdRes id: Int, args: Parcelable? = null) {
        findNavController().navigate(id, Bundle().apply { putParcelable(MvRx.KEY_ARG, args) })
    }
}