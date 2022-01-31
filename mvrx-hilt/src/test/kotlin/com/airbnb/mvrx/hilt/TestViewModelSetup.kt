package com.airbnb.mvrx.hilt

import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

data class TestState(
    val data: String = "",
) : MavericksState

class TestViewModel @AssistedInject constructor(
    @Assisted initialState: TestState,
) : MavericksViewModel<TestState>(initialState) {

    fun setData(data: String) = setState {
        copy(data = data)
    }

    @AssistedFactory
    interface Factory : AssistedViewModelFactory<TestViewModel, TestState> {
        override fun create(state: TestState): TestViewModel
    }

    companion object : MavericksViewModelFactory<TestViewModel, TestState> by hiltMavericksViewModelFactory()
}
