package com.airbnb.mvrx.dogs

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.dogs.data.Dog

data class DogsState(
    val dogs: Async<List<Dog>> = Uninitialized,
    val lovedDogId: Long? = null,
    val adoptionRequest: Async<Dog> = Uninitialized
) : MvRxState {
    val lovedDog = dogs()?.firstOrNull() { it.id == lovedDogId }

    fun dog(id: Long) = dogs()?.firstOrNull { it.id == id }  ?: throw IllegalArgumentException("Unknown dog $id")
}