package com.airbnb.mvrx.news.di

import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Module

@AssistedModule
@Module(includes = [AssistedInject_AppModule::class, ServiceModule::class])
object AppModule