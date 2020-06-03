package com.airbnb.mvrx.sample.views

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import com.airbnb.epoxy.ModelView
import com.airbnb.epoxy.TextProp
import com.airbnb.mvrx.sample.R
import com.airbnb.mvrx.sample.databinding.MarqueeBinding
import com.airbnb.mvrx.sample.utils.viewBinding

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class Marquee @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private val binding by viewBinding<MarqueeBinding>()

    init {
        orientation = VERTICAL
        context.withStyledAttributes(attrs, R.styleable.Marquee) {
            if (hasValue(R.styleable.Marquee_android_title)) setTitle(getText(R.styleable.Marquee_android_title))
        }
    }

    @TextProp
    fun setTitle(title: CharSequence) {
        binding.title.text = title
    }

    @TextProp
    fun setSubtitle(subtitle: CharSequence?) {
        binding.subtitle.isVisible = subtitle.isNullOrBlank().not()
        binding.subtitle.text = subtitle
    }
}
