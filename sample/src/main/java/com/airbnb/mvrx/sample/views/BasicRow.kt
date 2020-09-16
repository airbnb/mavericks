package com.airbnb.mvrx.sample.views

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelView
import com.airbnb.epoxy.TextProp
import com.airbnb.mvrx.sample.databinding.BasicRowBinding
import com.airbnb.mvrx.sample.utils.viewBinding

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class BasicRow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private val binding: BasicRowBinding by viewBinding()

    init {
        orientation = VERTICAL
    }

    @TextProp
    fun setTitle(title: CharSequence?) {
        binding.title.text = title
    }

    @TextProp
    fun setSubtitle(subtitle: CharSequence?) {
        binding.subtitle.isVisible = subtitle.isNullOrBlank().not()
        binding.subtitle.text = subtitle
    }

    @CallbackProp
    fun setClickListener(clickListener: OnClickListener?) {
        setOnClickListener(clickListener)
    }
}
