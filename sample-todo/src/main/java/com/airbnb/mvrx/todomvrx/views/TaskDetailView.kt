package com.airbnb.mvrx.todomvrx.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.RelativeLayout
import android.widget.TextView
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.airbnb.mvrx.todomvrx.data.Task
import com.airbnb.mvrx.todomvrx.todoapp.R

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_MATCH_HEIGHT)
class TaskDetailView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private val titleView by lazy { findViewById<TextView>(R.id.task_title) }
    private val descriptionView by lazy { findViewById<TextView>(R.id.task_description) }
    private val checkbox by lazy { findViewById<CheckBox>(R.id.complete) }
    private val noDataView by lazy { findViewById<View>(R.id.no_data_view) }

    init {
        inflate(context, R.layout.task_detail, this)
    }

    @ModelProp
    fun setTask(task: Task?) {
        titleView.text = task?.title
        descriptionView.text = task?.description
        checkbox.isChecked = task?.complete ?: false
        checkbox.visibility = if (task == null) View.GONE else View.VISIBLE
        noDataView.visibility = if (task == null) View.VISIBLE else View.GONE
    }

    @CallbackProp
    fun onCheckedChanged(listener: CompoundButton.OnCheckedChangeListener?) {
        checkbox.setOnCheckedChangeListener(listener)
    }
}
