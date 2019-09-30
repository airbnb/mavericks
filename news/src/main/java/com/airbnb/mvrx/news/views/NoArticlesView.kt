package com.airbnb.mvrx.news.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import com.airbnb.epoxy.ModelView
import com.airbnb.mvrx.news.R

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class NoArticlesView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val message: TextView

    init {
        inflate(context, R.layout.item_message, this)
        message = findViewById(R.id.message)
        message.text = context.getString(R.string.noArticlesToShowText)
    }
}
