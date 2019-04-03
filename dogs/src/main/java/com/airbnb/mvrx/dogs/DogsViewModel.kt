package com.airbnb.mvrx.dogs

import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.airbnb.mvrx.dogs.app.DogApplication
import com.airbnb.mvrx.dogs.app.MvRxViewModel
import com.airbnb.mvrx.dogs.data.Dog
import com.airbnb.mvrx.dogs.data.DogRepository
import io.reactivex.schedulers.Schedulers

class DogsViewModel(
    state: DogsState,
    private val dogRepository: DogRepository
) : MvRxViewModel<DogsState>(state) {

    init {
        dogRepository.getDogs()
            .subscribeOn(Schedulers.io())
            .execute { copy(dogs = it) }
    }

    fun loveDog(dogId: Long) = setState { copy(lovedDogId = dogId) }

    fun adoptLovedDog() = withState  { state ->
        dogRepository.adoptDogs(state.lovedDog ?: return@withState)
            .subscribeOn(Schedulers.io())
            .execute { copy(adoptionRequest = it) }
    }

    companion object : MvRxViewModelFactory<DogsViewModel, DogsState> {
        override fun create(viewModelContext: ViewModelContext, state: DogsState): DogsViewModel? {
            return DogsViewModel(state, viewModelContext.app<DogApplication>().dogsRespository)
        }
    }
}