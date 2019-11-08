package com.airbnb.mvrx.sample.dadjoke

import com.airbnb.mvrx.sample.dadjoke.network.DadJokeHomeService
import com.airbnb.mvrx.sample.dadjoke.network.DadJokeService
import org.koin.core.context.loadKoinModules

import retrofit2.Retrofit
import org.koin.dsl.module

private fun dadJokeServiceModule() = module {
    single(override=true) {
        get<Retrofit>().create(DadJokeService::class.java)
    }
}

private fun dadJokeHomeServiceModule() = module {
    single(override=true) {
        get<Retrofit>().create(DadJokeHomeService::class.java)
    }
}

fun install () {
    loadKoinModules(dadJokeServiceModule())
}

fun installHome () {
    loadKoinModules(dadJokeHomeServiceModule())
}
