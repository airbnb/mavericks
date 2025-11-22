package com.airbnb.mvrx.dogs

import android.os.Bundle
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.PersistState
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.dogs.data.Dog

data class DogDetailState(
    @PersistState
    val dogId: Long,
    val dog: Async<Dog> = Uninitialized,
) : MavericksState {
    constructor(arguments: Bundle) : this(dogId = arguments.getLong(DogDetailFragment.ARG_DOG_ID))
}