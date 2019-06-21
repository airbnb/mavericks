package com.airbnb.mvrx.sample.features.statefulview

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import com.airbnb.mvrx.StatefulView
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState

class FragmentCounterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextView(context, attrs, defStyleAttr), StatefulView {

    private val viewModel: CounterViewModel by fragmentViewModel()

    override fun onInvalidate() = withState(viewModel) { state ->
        text = "Fragment Count: ${state.count}"
    }
}