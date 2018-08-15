package com.airbnb.mvrx.todomvrx.views

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import android.widget.ScrollView
import com.airbnb.epoxy.ModelView
import com.airbnb.epoxy.TextProp
import com.airbnb.mvrx.todomvrx.todoapp.R

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_MATCH_HEIGHT)
class AddEditView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ScrollView(context, attrs, defStyleAttr) {

    private val titleView by lazy { findViewById<EditText>(R.id.task_title) }
    private val descriptionView by lazy { findViewById<EditText>(R.id.task_description) }

    init {
        inflate(context, R.layout.add_edit_view, this)
    }

    @TextProp
    fun setTitle(title: CharSequence?) {
        titleView.setText(title)
    }

    @TextProp
    fun setDescription(description: CharSequence?) {
        descriptionView.setText(description)
    }

    fun data() = titleView.text.toString() to descriptionView.text.toString()
}