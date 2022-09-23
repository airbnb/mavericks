package com.airbnb.mvrx.sample.anvil.di

import javax.inject.Scope
import kotlin.reflect.KClass

/**
 * This annotation lets you use the same annotation to represent which scope you want to contribute an Anvil object to
 * and also as `@SingleIn(YourScope::class)`.
 *
 * Without `@SingleIn`, an AppComponent contribution might look like this:
 * ```
 * @Singleton
 * @ContributesBinding(AppScope::class)
 * class YourClassImpl : YourClass
 * ```
 * Singleton is a well defined pattern for AppScope but the scope naming becomes more confusing once you start defining your
 * own components.
 * `@SingleIn` prevents you from memorizing two names per component. The above example becomes:
 * ```
 * @SingleIn(AppScope::class)
 * @ContributesBinding(AppScope::class)
 * class YourClassImpl : YourClass
 * ```
 *
 * And custom components would look like:
 * ```
 * @SingleIn(YourScope::class)
 * @ContributesBinding(YourScope::class)
 * class YourClassImpl : YourClass
 * ```
 */
@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class SingleIn(val clazz: KClass<*>)
