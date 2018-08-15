package com.airbnb.mvrx.todomvrx

import android.os.Bundle
import android.view.View
import com.airbnb.epoxy.EpoxyController
import com.airbnb.mvrx.todomvrx.core.BaseFragment
import com.airbnb.mvrx.todomvrx.todoapp.R
import com.airbnb.mvrx.todomvrx.views.statisticsView
import com.airbnb.mvrx.withState

class StatisticsFragment : BaseFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fab.visibility = View.GONE
    }

    override fun EpoxyController.buildModels() = withState(viewModel) { state ->

        if (state.tasks.isEmpty()) {
            statisticsView {
                id("no_tasks")
                statistic(R.string.statistics_no_tasks)
            }
            return@withState
        }

        val (completeTasks, activeTasks) = state.tasks.partition { it.complete }
        statisticsView {
            id("active")
            statistic(getString(R.string.statistics_active_tasks, activeTasks.size))
        }
        statisticsView {
            id("complete")
            statistic(getString(R.string.statistics_completed_tasks, completeTasks.size))
        }
    }
}
