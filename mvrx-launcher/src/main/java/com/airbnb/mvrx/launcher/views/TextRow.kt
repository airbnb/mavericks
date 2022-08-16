package com.airbnb.mvrx.launcher.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isGone
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelView
import com.airbnb.epoxy.TextProp
import com.airbnb.mvrx.launcher.R

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class TextRow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val titleView: TextView
    private val subtitleView: TextView

    init {
        inflate(context, R.layout.mavericks_text_row, this)
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
        subtitleView.isGone = subtitle.isNullOrBlank()
        subtitleView.text = subtitle
    }

    @CallbackProp
    fun onClickListener(listener: View.OnClickListener?) {
        isClickable = listener != null
        setOnClickListener(listener)
    }
}
