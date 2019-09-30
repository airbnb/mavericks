package com.airbnb.mvrx.news.usecases

import com.airbnb.mvrx.news.models.HeadlinesResponse
import com.airbnb.mvrx.news.service.NewsService
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class GetHeadlinesUseCase @Inject constructor(
        private val newsService: NewsService
) {

    fun getHeadlines(page: Int): Single<HeadlinesResponse> {
        return newsService.getHeadlines(page).subscribeOn(Schedulers.io())
    }

}