package com.airbnb.mvrx.news.di

import android.content.Context
import com.airbnb.mvrx.news.service.Config
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton
import java.util.Date

@Module
object NetworkModule {

    private const val CACHE_SIZE = 102400L

    @Provides
    @Singleton
    @JvmStatic
    fun provideRetrofit(context: Context): Retrofit {
        val cache = Cache(context.cacheDir, CACHE_SIZE)

        val interceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .cache(cache)
                .build()

        val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .add(Date::class.java, Rfc3339DateJsonAdapter())
                .build()

        return Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .client(okHttpClient)
                .baseUrl(Config.BASE_URL)
                .build()
    }
}

