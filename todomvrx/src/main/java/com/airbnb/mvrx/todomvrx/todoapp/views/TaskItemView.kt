package com.airbnb.mvrx.todomvrx.todoapp.views

import android.content.Context
import android.util.AttributeSet
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.airbnb.epoxy.TextProp
import com.airbnb.mvrx.todomvrx.todoapp.R

@ModelView(autoLayout = ModelView.Size.WRAP_WIDTH_MATCH_HEIGHT)
class TaskItemView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val container by lazy { findViewById<LinearLayout>(R.id.container) }
    private val checkbox by lazy { findViewById<CheckBox>(R.id.checkbox) }
    private val titleView by lazy { findViewById<TextView>(R.id.title) }

    init {
        inflate(context, R.layout.task_item, this)
    }

    @TextProp
    fun setTitle(title: CharSequence) {
        titleView.text = title
    }

    @ModelProp
    fun setChecked(checked: Boolean) {
        checkbox.isChecked = checked
    }

    @CallbackProp
    fun onCheckedChanged(callback: ((Boolean) -> Unit)?) {
        callback?.let { _ ->
            container.setOnClickListener { _ ->
                callback.invoke(!checkbox.isChecked)
            }
        }
    }
}