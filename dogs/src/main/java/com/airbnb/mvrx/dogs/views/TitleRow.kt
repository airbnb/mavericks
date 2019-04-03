package com.airbnb.mvrx.dogs.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelView
import com.airbnb.epoxy.TextProp
import com.airbnb.mvrx.dogs.R
import kotlinx.android.synthetic.main.title_row.view.titleView

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class TitleRow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    
    init {
        LayoutInflater.from(context).inflate(R.layout.title_row, this, true)
    }

    @TextProp
    fun setTitle(title: CharSequence) {
        titleView.text = title
    }
}