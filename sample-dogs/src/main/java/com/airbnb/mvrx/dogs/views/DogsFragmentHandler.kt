package com.airbnb.mvrx.dogs.views

import com.airbnb.mvrx.dogs.data.Dog

interface DogsFragmentHandler {
    fun onDogClicked(dog: Dog)
    fun adoptLovedDog()
}
