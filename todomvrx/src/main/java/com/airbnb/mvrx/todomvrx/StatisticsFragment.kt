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

import android.os.Bundle
import android.view.View
import com.airbnb.epoxy.EpoxyController
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.todomvrx.core.BaseFragment
import com.airbnb.mvrx.todomvrx.views.statisticsView
import com.airbnb.mvrx.withState

class StatisticsFragment : BaseFragment() {

    private val viewModel by activityViewModel(TasksViewModel::class)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fab.visibility = View.GONE
    }

    override fun EpoxyController.buildModels() = withState(viewModel) { state ->
        val (completeTasks, activeTasks) = state.tasks.partition { it.complete }
        statisticsView {
            id("active")
            statistic("Active tasks: ${activeTasks.size}")
        }
        statisticsView {
            id("complete")
            statistic("Complete tasks: ${completeTasks.size}")
        }
    }
}
