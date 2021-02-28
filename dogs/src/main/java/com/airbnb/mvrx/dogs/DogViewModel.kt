package com.airbnb.mvrx.dogs

import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.airbnb.mvrx.dogs.data.DogRepository
import com.airbnb.mvrx.dogs.utils.MvRxViewModel
import io.reactivex.schedulers.Schedulers

class DogViewModel(
    initialState: DogState,
    private val dogRepository: DogRepository
) : MvRxViewModel<DogState>(initialState) {

    init {
        dogRepository.getDogs().execute { copy(dogs = it) }
    }

    fun loveDog(dogId: Long) = setState { copy(lovedDogId = dogId) }

    fun adoptLovedDog() = withState { state ->
        val lovedDog = state.lovedDog ?: throw IllegalStateException("You must love a dog first!")
        dogRepository.adoptDog(lovedDog)
            .subscribeOn(Schedulers.io())
            .execute { copy(adoptionRequest = it) }
    }

    companion object : MavericksViewModelFactory<DogViewModel, DogState> {
        override fun create(viewModelContext: ViewModelContext, state: DogState): DogViewModel {
            val dogRepository = viewModelContext.app<DogApplication>().dogsRepository
            return DogViewModel(state, dogRepository)
        }
    }
}
