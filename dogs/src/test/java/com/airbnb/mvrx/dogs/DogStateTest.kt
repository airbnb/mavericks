package com.airbnb.mvrx.dogs

import com.airbnb.mvrx.Success
import com.airbnb.mvrx.dogs.data.Dog
import org.junit.Assert.assertEquals
import org.junit.Test

class DogStateTest {
    @Test
    fun testLovedDog() {
        val dog = Dog(123, "", "", "", "")
        val state = DogState(dogs = Success(listOf(dog)), lovedDogId = dog.id)
        assertEquals(dog, state.lovedDog)
    }
}