package com.airbnb.mvrx.news

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.news.models.Article
import com.airbnb.mvrx.news.models.HeadlinesResponse

data class HeadlinesState(
        val headlinesRequest: Async<HeadlinesResponse> = Uninitialized,
        val headlines: List<Article> = emptyList(),
        val page: Int = 0
) : MvRxState {
    val hasMoreArticles: Boolean
        get() {
            val totalResults = headlinesRequest()?.totalResults ?: Int.MAX_VALUE
            return headlines.size < totalResults
        }
}