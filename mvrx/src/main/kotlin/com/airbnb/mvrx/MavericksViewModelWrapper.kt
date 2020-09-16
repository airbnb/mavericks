package com.airbnb.mvrx

import androidx.lifecycle.ViewModel

class MavericksViewModelWrapper<VM : MavericksViewModel<S>, S : MavericksState>(val viewModel: VM) : ViewModel() {
    override fun onCleared() {
        super.onCleared()
        viewModel.onCleared()
    }
}
