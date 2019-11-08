package com.airbnb.mvrx.sample.dadjoke.network

import com.airbnb.mvrx.sample.dadjoke.models.Joke
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Headers

interface DadJokeHomeService {
    @Headers("Accept: application/json")
    @GET("/")
    fun random(): Observable<Joke>
}