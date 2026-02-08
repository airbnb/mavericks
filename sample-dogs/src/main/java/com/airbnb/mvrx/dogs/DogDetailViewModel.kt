package com.airbnb.mvrx.dogs

import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.ViewModelContext
import com.airbnb.mvrx.dogs.data.DogRepository
import com.airbnb.mvrx.dogs.utils.MvRxViewModel

class DogDetailViewModel(
    initialState: DogDetailState,
    private val dogRepository: DogRepository
) : MvRxViewModel<DogDetailState>(initialState) {

    init {
        withState {
            getDog(it.dogId)
        }
    }

    private fun getDog(dogId: Long) {
        dogRepository.getDog(dogId)
            .doOnSubscribe { setState { copy(dog = Loading()) } }
            .subscribe { dog ->
                dog?.let {
                    setState { copy(dog = Success(it)) }
                } ?: setState { copy(dog = Fail(Throwable("Dog $dogId not found :("))) }
            }.disposeOnClear()
    }

    companion object : MavericksViewModelFactory<DogDetailViewModel, DogDetailState> {

        override fun create(viewModelContext: ViewModelContext, state: DogDetailState): DogDetailViewModel {
            val dogRepository = viewModelContext.app<DogApplication>().dogsRepository
            return DogDetailViewModel(state, dogRepository)
        }
    }
}