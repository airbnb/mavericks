package com.airbnb.mvrx.sample.dadjoke.network

import com.airbnb.mvrx.sample.dadjoke.models.Joke
import com.airbnb.mvrx.sample.dadjoke.models.JokesResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface DadJokeService {
    @Headers("Accept: application/json")
    @GET("search")
    fun search(
        @Query("query") query: String? = null,
        @Query("page") page: Int = 0,
        @Query("limit") limit: Int = 20
    ): Observable<JokesResponse>

    @Headers("Accept: application/json")
    @GET("j/{id}")
    fun fetch(@Path("id") id: String): Observable<Joke>

    @Headers("Accept: application/json")
    @GET("/")
    fun random(): Observable<Joke>
}