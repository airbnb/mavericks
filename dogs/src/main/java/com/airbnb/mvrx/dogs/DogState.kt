package com.airbnb.mvrx.dogs

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.dogs.data.Dog

data class DogState(
    val dogs: Async<List<Dog>> = Uninitialized,
    val lovedDogId: Long? = null,
    val adoptionRequest: Async<Dog> = Uninitialized
) : MvRxState {
    val lovedDog: Dog? = dog(lovedDogId)

    fun dog(dogId: Long?): Dog? = dogs()?.firstOrNull { it.id == dogId }
}