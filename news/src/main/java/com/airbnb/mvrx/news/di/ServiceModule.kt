package com.airbnb.mvrx.news.di

import com.airbnb.mvrx.news.service.NewsService
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

@Module(includes = [NetworkModule::class])
object ServiceModule {
    @Singleton
    @Provides
    @JvmStatic
    fun newsService(retrofit: Retrofit): NewsService {
        return retrofit.create(NewsService::class.java)
    }
}