package com.airbnb.mvrx.sample.features.statefulview

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelView
import com.airbnb.epoxy.TextProp
import com.airbnb.mvrx.StatefulView
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.sample.R
import com.airbnb.mvrx.withState

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class StatefulBasicRow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), StatefulView {

    private val fragmentScopedViewModel: StatefulViewViewModel by fragmentViewModel()
    private val activityScopedViewModel: StatefulViewViewModel by activityViewModel()

    private val titleView: TextView
    private val subtitleView: TextView

    init {
        inflate(context, R.layout.basic_row, this)
        titleView = findViewById(R.id.title)
        subtitleView = findViewById(R.id.subtitle)
        orientation = VERTICAL
    }

    @TextProp
    fun setTitle(title: CharSequence) {
        titleView.text = title
    }

    @CallbackProp
    fun setClickListener(clickListener: OnClickListener?) {
        setOnClickListener(clickListener)
    }

    override fun onInvalidate() = withState(fragmentScopedViewModel, activityScopedViewModel) { fragmentState, activityState ->
        subtitleView.text = "${fragmentState.title}\n${activityState.title}"
    }
}