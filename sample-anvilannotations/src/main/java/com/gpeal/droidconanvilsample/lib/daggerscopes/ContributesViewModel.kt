package com.gpeal.droidconanvilsample.lib.daggerscopes

import kotlin.reflect.KClass

/**
 * Adds view model to the specified component graph.
 * Equivalent to the following declaration in a dagger module:
 *
 *     @Binds
 *     @IntoMap
 *     @ViewModelKey(YourViewModel::class)
 *     public abstract fun bindYourViewModelFactory(factory: YourViewModel.Factory): TonalViewModelFactory<*, *>
 */
@Target(AnnotationTarget.CLASS)
annotation class ContributesViewModel(
    val scope: KClass<*>,
)