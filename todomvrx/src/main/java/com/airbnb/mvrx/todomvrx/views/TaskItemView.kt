package com.airbnb.mvrx.todomvrx.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.TextView
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.airbnb.epoxy.TextProp
import com.airbnb.mvrx.todomvrx.todoapp.R

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class TaskItemView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val clickOverlay by lazy { findViewById<View>(R.id.click_overlay) }
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
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                callback.invoke(isChecked)
            }
        }
    }

    @CallbackProp
    fun onClickListener(listener: View.OnClickListener?) {
        clickOverlay.setOnClickListener(listener)
    }
}