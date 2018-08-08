package com.airbnb.mvrx.sample.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Joke(val id: String, val joke: String)