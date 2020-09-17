package com.airbnb.mvrx.sample.features.dadjoke.mocks

import com.airbnb.mvrx.Success
import com.airbnb.mvrx.sample.features.dadjoke.RandomDadJokeState
import com.airbnb.mvrx.sample.models.Joke
import kotlin.String

val mockRandomDadJokeState by lazy { RandomDadJokeState(
joke = Success(
value = Joke(
id = "jNmykbUSSvc",
joke = "I went to a book store and asked the saleswoman where the Self Help section was, she said if she told me it would defeat the purpose."
)
)
) }
