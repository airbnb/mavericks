package com.airbnb.mvrx.dogs

import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.airbnb.mvrx.dogs.app.DogApplication
import com.airbnb.mvrx.dogs.app.MvRxViewModel
import com.airbnb.mvrx.dogs.data.DogRepository
import io.reactivex.schedulers.Schedulers

class DogViewModel(
    state: DogState,
    private val dogRepository: DogRepository
) : MvRxViewModel<DogState>(state) {

    init {
        dogRepository.getDogs()
            .subscribeOn(Schedulers.io())
            .execute { copy(dogs = it) }
    }

    fun loveDog(dogId: Long) = setState { copy(lovedDogId = dogId) }

    fun adoptLovedDog() = withState { state ->
        val lovedDog = state.lovedDog ?: throw IllegalStateException("You must love a dog first!")
        dogRepository.adoptDog(lovedDog)
            .subscribeOn(Schedulers.io())
            .execute { copy(adoptionRequest = it) }
    }

    companion object : MvRxViewModelFactory<DogViewModel, DogState> {
        override fun create(viewModelContext: ViewModelContext, state: DogState): DogViewModel? {
            val dogRepository = viewModelContext.app<DogApplication>().dogsRespository
            return DogViewModel(state, dogRepository)
        }
    }
}