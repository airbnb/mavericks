package com.airbnb.mvrx.sample.core

import android.app.Application
import com.airbnb.mvrx.launcher.MavericksLauncherMockActivity
import com.airbnb.mvrx.mocking.MockableMavericks
import com.airbnb.mvrx.sample.LauncherActivity
import com.airbnb.mvrx.sample.network.DadJokeService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

class MvRxApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        MockableMavericks.initialize(this)
        // Override the default activity for showing mocks from the launcher
        MavericksLauncherMockActivity.activityToShowMock = LauncherActivity::class

        startKoin {
            androidContext(this@MvRxApplication)
            modules(dadJokeServiceModule)
        }
    }
}

private val dadJokeServiceModule = module {
    factory {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    factory {
        Retrofit.Builder()
            .baseUrl("https://icanhazdadjoke.com/")
            .addConverterFactory(MoshiConverterFactory.create(get<Moshi>()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }

    single {
        get<Retrofit>().create(DadJokeService::class.java)
    }
}
