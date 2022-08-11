package com.airbnb.mvrx.todomvrx

import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuProvider
import androidx.navigation.fragment.findNavController
import com.airbnb.mvrx.args
import com.airbnb.mvrx.todomvrx.core.BaseFragment
import com.airbnb.mvrx.todomvrx.data.findTask
import com.airbnb.mvrx.todomvrx.todoapp.R
import com.airbnb.mvrx.todomvrx.util.simpleController
import com.airbnb.mvrx.todomvrx.views.taskDetailView
import kotlinx.parcelize.Parcelize

@Parcelize
data class TaskDetailArgs(val id: String) : Parcelable

/**
 * Main UI for the task detail screen.
 */
class TaskDetailFragment : BaseFragment() {

    private val args: TaskDetailArgs by args()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.taskdetail_fragment_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_delete -> {
                        viewModel.deleteTask(args.id)
                        findNavController().navigateUp()
                        true
                    }
                    else -> false
                }
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fab.setImageResource(R.drawable.ic_edit)
        fab.setOnClickListener {
            navigate(R.id.addEditFragment, AddEditTaskArgs(args.id))
        }
    }

    override fun epoxyController() = simpleController(viewModel) { state ->
        taskDetailView {
            id("detail")
            task(state.tasks.findTask(args.id))
            onCheckedChanged { _, checked -> viewModel.setComplete(args.id, checked) }
        }
    }
}
