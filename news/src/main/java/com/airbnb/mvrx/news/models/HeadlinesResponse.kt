package com.airbnb.mvrx.news.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HeadlinesResponse(
        val status: String,
        val totalResults: Int,
        val articles: List<Article>
)
