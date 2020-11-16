package com.airbnb.mvrx.sample.features.dadjoke.mocks

import com.airbnb.mvrx.Success
import com.airbnb.mvrx.sample.features.dadjoke.RandomDadJokeState
import com.airbnb.mvrx.sample.models.Joke

val mockRandomDadJokeState by lazy {
    RandomDadJokeState(
        joke = Success(
            value = Joke(
                id = "ozPmbFtWDlb",
                joke = "Some people say that comedians who tell one too many light bulb jokes soon burn out, but they don't " +
                    "know watt they are talking about. They're not that bright."
            )
        )
    )
}
