package com.airbnb.mvrx.sample.features.statefulview

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.TextView
import com.airbnb.mvrx.StatefulView
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.withState

class ActivityCounterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextView(context, attrs, defStyleAttr), StatefulView {

    private val viewModel: CounterViewModel by activityViewModel()

    override fun onInvalidate() = withState(viewModel) { state ->
        Log.d("Gabe", "onInvalidate: ActivityCounterView")
        text = "Activity Count: ${state.count}"
    }
}