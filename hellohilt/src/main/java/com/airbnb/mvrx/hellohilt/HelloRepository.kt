package com.airbnb.mvrx.hellohilt

import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class HelloRepository @Inject constructor() {

    fun sayHello(): Observable<String> {
        return Observable
            .just("Hello, world!")
            .delay(2, TimeUnit.SECONDS)
    }
}