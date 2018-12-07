package com.airbnb.mvrx.sample.models

import com.squareup.moshi.Json

data class JokesResponse(
        @get:Json(name = "next_page") @Json(name = "next_page") val nextPage: Int,
        @get:Json(name = "results") @Json(name = "results") val results: List<Joke>
)