package com.airbnb.mvrx.news.service

import com.airbnb.mvrx.news.BuildConfig
import com.airbnb.mvrx.news.models.HeadlinesResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsService {

    @GET("top-headlines")
    fun getHeadlines(
            @Query("page") page: Int,
            @Query("pageSize") pageSize: Int = Config.PAGE_SIZE,
            @Query("country") country: String = "us",
            @Query("apiKey") key: String = BuildConfig.ApiKey
    ): Single<HeadlinesResponse>

}