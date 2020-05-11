package com.airbnb.mvrx

import androidx.lifecycle.ViewModel

class MvRxViewModelWrapper<VM : BaseMvRxViewModel<S>, S : MvRxState>(val viewModel: VM) : ViewModel() {
    override fun onCleared() {
        super.onCleared()
        viewModel.onCleared()
    }
}