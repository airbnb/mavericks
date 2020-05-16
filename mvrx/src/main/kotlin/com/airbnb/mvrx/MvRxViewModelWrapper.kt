package com.airbnb.mvrx

import androidx.lifecycle.ViewModel

class MvRxViewModelWrapper<VM : BaseMavericksViewModel<S>, S : MvRxState>(val viewModel: VM) : ViewModel() {
    override fun onCleared() {
        super.onCleared()
        viewModel.onCleared()
    }
}