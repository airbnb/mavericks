package com.airbnb.mvrx.sample.models

import com.squareup.moshi.Json

data class JokesResponse(
    @Json(name = "next_page") val nextPage: Int,
    val results: List<Joke>
)
