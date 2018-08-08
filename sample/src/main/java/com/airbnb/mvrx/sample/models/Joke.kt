package com.airbnb.mvrx.sample.models

import com.squareup.moshi.Json

data class Joke(
        @Json(name = "id") val id: String,
        @Json(name = "joke") val joke: String
)