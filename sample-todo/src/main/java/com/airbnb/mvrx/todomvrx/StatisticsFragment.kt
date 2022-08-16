package com.airbnb.mvrx.todomvrx

import android.os.Bundle
import android.view.View
import com.airbnb.mvrx.todomvrx.core.BaseFragment
import com.airbnb.mvrx.todomvrx.todoapp.R
import com.airbnb.mvrx.todomvrx.util.simpleController
import com.airbnb.mvrx.todomvrx.views.statisticsView

class StatisticsFragment : BaseFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fab.hide()
    }

    override fun epoxyController() = simpleController(viewModel) { state ->

        if (state.tasks.isEmpty()) {
            statisticsView {
                id("no_tasks")
                statistic(R.string.statistics_no_tasks)
            }
            return@simpleController
        }

        val (completeTasks, activeTasks) = state.tasks.partition { it.complete }
        statisticsView {
            id("active")
            val size: Int = activeTasks.size
            statistic(getString(R.string.statistics_active_tasks, size))
        }
        statisticsView {
            id("complete")
            val size: Int = completeTasks.size
            statistic(getString(R.string.statistics_completed_tasks, size))
        }
    }
}
