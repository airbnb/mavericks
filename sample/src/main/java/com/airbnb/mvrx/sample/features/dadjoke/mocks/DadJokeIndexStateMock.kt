package com.airbnb.mvrx.sample.features.dadjoke.mocks

import com.airbnb.mvrx.Success
import com.airbnb.mvrx.sample.features.dadjoke.DadJokeIndexState
import com.airbnb.mvrx.sample.models.Joke
import com.airbnb.mvrx.sample.models.JokesResponse

val mockDadJokeIndexState by lazy {
    DadJokeIndexState(
        jokes = listOf(
            Joke(
                id = "0189hNRf2g",
                joke = "I'm tired of following my dreams. I'm just going to ask them where they are going and meet up with them later."
            ),
            Joke(
                id = "08EQZ8EQukb",
                joke = "Did you hear about the guy whose whole left side was cut off? He's all right now."
            ),
            Joke(
                id = "08xHQCdx5Ed",
                joke = "Why didn’t the skeleton cross the road? Because he had no guts."
            )
        ),
        request = Success(
            value = JokesResponse(
                nextPage = 3,
                results = listOf(
                    Joke(
                        id = "0LuXvkq4Muc",
                        joke = "I knew I shouldn't steal a mixer from work, but it was a whisk I was willing to take."
                    ),
                    Joke(
                        id = "0ga2EdN7prc",
                        joke = "How come the stadium got hot after the game? Because all of the fans left."
                    ),
                    Joke(
                        id = "0oO71TSv4Ed",
                        joke = "Why was it called the dark ages? Because of all the knights. "
                    )
                )
            )
        )
    )
}
