package com.airbnb.mvrx.sample.models

import com.squareup.moshi.Json

data class Joke(
        @get:Json(name = "id") @Json(name = "id") val id: String,
        @get:Json(name = "joke") @Json(name = "joke") val joke: String
)