package com.airbnb.mvrx.dogs

import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext

class DogsViewModel(
    state: DogsState,
    dogRepository: DogRepository
) : MvRxViewModel<DogsState>(state) {

    init {
        dogRepository.getDogs().execute { copy(dogs = it) }
    }

    companion object : MvRxViewModelFactory<DogsViewModel, DogsState> {
        override fun create(viewModelContext: ViewModelContext, state: DogsState): DogsViewModel? {
            return DogsViewModel(state, viewModelContext.app<DogApplication>().dogsRespository)
        }
    }
}