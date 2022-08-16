package com.airbnb.mvrx.launcher.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.airbnb.epoxy.ModelView
import com.airbnb.epoxy.TextProp
import com.airbnb.mvrx.launcher.R

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class Marquee @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val titleView: TextView
    private val subtitleView: TextView

    init {
        inflate(context, R.layout.mavericks_marquee, this)
        titleView = findViewById(R.id.title)
        subtitleView = findViewById(R.id.subtitle)
        orientation = VERTICAL
    }

    @TextProp
    fun setTitle(title: CharSequence) {
        titleView.text = title
    }

    @TextProp
    fun setSubtitle(subtitle: CharSequence?) {
        subtitleView.visibility = if (subtitle.isNullOrBlank()) View.GONE else View.VISIBLE
        subtitleView.text = subtitle
    }
}
