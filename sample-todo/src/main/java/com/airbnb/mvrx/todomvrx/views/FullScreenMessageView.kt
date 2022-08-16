package com.airbnb.mvrx.todomvrx.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.airbnb.epoxy.TextProp
import com.airbnb.mvrx.todomvrx.todoapp.R

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_MATCH_HEIGHT)
class FullScreenMessageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val iconView by lazy { findViewById<ImageView>(R.id.icon) }
    private val titleView by lazy { findViewById<TextView>(R.id.title) }

    init {
        inflate(context, R.layout.full_screen_message, this)
    }

    @ModelProp
    fun setIconRes(@DrawableRes drawableRes: Int) {
        iconView.setImageResource(drawableRes)
    }

    @TextProp
    fun setTitle(title: CharSequence) {
        titleView.text = title
    }
}
