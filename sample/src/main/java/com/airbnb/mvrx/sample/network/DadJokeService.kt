package com.airbnb.mvrx.sample.network

import com.airbnb.mvrx.sample.models.Joke
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface DadJokeService {
    @GET("search")
    fun search(
            @Query("query") query: String? = null,
            @Query("page") page: Int = 0,
            @Query("limit") limit: Int = 20
    ): Observable<List<Joke>>
}