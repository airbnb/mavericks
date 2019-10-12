package com.airbnb.mvrx.helloDagger

import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Module

@AssistedModule
@Module(includes = [AssistedInject_AppModule::class])
object AppModule