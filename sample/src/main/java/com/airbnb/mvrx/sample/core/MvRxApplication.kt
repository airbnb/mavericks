package com.airbnb.mvrx.sample.core

import android.app.Application
import android.os.Handler
import android.os.HandlerThread
import com.airbnb.epoxy.EpoxyController
import com.airbnb.mvrx.sample.network.DadJokeService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.koin.android.ext.android.startKoin
import org.koin.dsl.module.Module
import org.koin.dsl.module.applicationContext
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory




class MvRxApplication : Application() {

    private val dadJokeServiceModule : Module = applicationContext {
        bean {
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val retrofit = Retrofit.Builder()
                    .baseUrl("https://icanhazdadjoke.com/")
                    .addConverterFactory(MoshiConverterFactory.create(moshi))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
            retrofit.create(DadJokeService::class.java)
        }
    }

    override fun onCreate() {
        super.onCreate()
        startKoin(this, listOf(dadJokeServiceModule))
    }
}