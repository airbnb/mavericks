package com.airbnb.mvrx.sample.anvil.annotation

import kotlin.reflect.KClass

/**
 * Adds view model to the specified component graph.
 * Equivalent to the following declaration in a dagger module:
 *
 *     @Binds
 *     @IntoMap
 *     @ViewModelKey(YourViewModel::class)
 *     public abstract fun bindYourViewModelFactory(factory: YourViewModel.Factory): AssistedViewModelFactory<*, *>
 */
@Target(AnnotationTarget.CLASS)
annotation class ContributesViewModel(
    val scope: KClass<*>,
)
