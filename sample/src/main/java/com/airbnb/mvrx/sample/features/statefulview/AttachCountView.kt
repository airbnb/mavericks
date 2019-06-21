package com.airbnb.mvrx.sample.features.statefulview

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.StatefulView
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.sample.core.MvRxViewModel
import com.airbnb.mvrx.withState

data class AttachCountState(val attachCount: Int = 0) : MvRxState
class AttachCountViewModel(state: AttachCountState) : MvRxViewModel<AttachCountState>(state) {
    fun incrementAttachCount() = setState { copy(attachCount = attachCount + 1) }
}

class AttachCountView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextView(context, attrs, defStyleAttr), StatefulView {

    private val viewModel: AttachCountViewModel by fragmentViewModel()
    private val activityCounterViewModel: CounterViewModel by activityViewModel()

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewModel.incrementAttachCount()
    }

    override fun onInvalidate() = withState(viewModel, activityCounterViewModel) { state, counterState ->
        text = "Attach Count: ${state.attachCount}\nActivity Count: ${counterState.count}"
    }
}