package com.airbnb.mvrx.hellohilt

import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.hellohilt.di.AssistedViewModelFactory
import com.airbnb.mvrx.hellohilt.di.hiltMavericksViewModelFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

data class HelloHiltState(
    val viewModelScopedClassId1: Int? = null,
    val viewModelScopedClassId2: Int? = null,
    val notViewModelScopedClassId1: Int? = null,
    val notViewModelScopedClassId2: Int? = null,
) : MvRxState

class HelloHiltViewModel @AssistedInject constructor(
    @Assisted state: HelloHiltState,
    private val repo1: HelloRepository,
    private val repo2: HelloRepository,
) : BaseMvRxViewModel<HelloHiltState>(state) {

    init {
        setState {
            copy(
                viewModelScopedClassId1 = repo1.viewModelScopedClass.id,
                viewModelScopedClassId2 = repo2.viewModelScopedClass.id,
                notViewModelScopedClassId1 = repo1.notViewModelScopedClass.id,
                notViewModelScopedClassId2 = repo2.notViewModelScopedClass.id,
            )
        }
    }

    @AssistedFactory
    interface Factory : AssistedViewModelFactory<HelloHiltViewModel, HelloHiltState> {
        override fun create(state: HelloHiltState): HelloHiltViewModel
    }

    companion object : MavericksViewModelFactory<HelloHiltViewModel, HelloHiltState> by hiltMavericksViewModelFactory()
}
