package com.airbnb.mvrx.weather

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import kotlinx.android.synthetic.main.text_fragment.text_view

data class WeatherState(val weather: Int = 72) : MvRxState

class WeatherViewModel(initialState: WeatherState) : MvRxViewModel<WeatherState>(initialState)

class WeatherFragment : BaseMvRxFragment(R.layout.text_fragment) {
    private val viewModel: WeatherViewModel by fragmentViewModel()

    override fun invalidate() = withState(viewModel) { state ->
    }
}