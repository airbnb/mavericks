package com.airbnb.mvrx.hellokoin

import io.reactivex.Observable
import java.util.concurrent.TimeUnit

class HelloRepository {

    fun sayHello(): Observable<String> {
        return Observable
            .just("Hello, world!")
            .delay(2, TimeUnit.SECONDS)
    }
}