package com.airbnb.mvrx.helloDagger.di

import com.airbnb.mvrx.helloDagger.base.BaseViewModel
import dagger.MapKey
import kotlin.reflect.KClass

/**
 * A [MapKey] for populating a map of ViewModels and their factories
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@MapKey
annotation class ViewModelKey(val value: KClass<out BaseViewModel<*>>)