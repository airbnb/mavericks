package com.airbnb.mvrx.sample.anvil.di

import android.content.Context
import android.content.ContextWrapper
import androidx.fragment.app.Fragment

/**
 * Use this to get the Dagger "Bindings" for your module. Bindings are used if you need to directly interact with a dagger component such as:
 * * an inject function: `inject(MyFragment frag)`
 * * an explicit getter: `fun myClass(): MyClass`
 *
 * Anvil will make your Dagger component implement these bindings so that you can call any of these functions on an instance of your component.
 *
 * [bindings] will walk up the Fragment/Activity hierarchy and check for [DaggerComponentOwner] to see if any of its components implement the
 * specified bindings. Most of the time this will "just work" and you don't have to think about it.
 *
 * For example, if your class has @Inject properties:
 * 1) Create an bindings interface such as `YourModuleBindings`
 * 1) Add an inject function like `fun inject(yourClass: YourClass)`
 * 2) Contribute your interface to the correct component via `@ContributesTo(AppScope::class)`.
 * 3) Call bindings<YourModuleBindings>().inject(this).
 */
inline fun <reified T : Any> Context.bindings() = bindings(T::class.java)

/**
 * @see bindings
 */
inline fun <reified T : Any> Fragment.bindings() = bindings(T::class.java)

/** Use no-arg extension function instead: [Context.bindings] */
fun <T : Any> Context.bindings(klass: Class<T>): T {
    // search dagger components in the context hierarchy
    return generateSequence(this) { (it as? ContextWrapper)?.baseContext }
        .plus(applicationContext)
        .filterIsInstance<DaggerComponentOwner>()
        .map { it.daggerComponent }
        .flatMap { if (it is Collection<*>) it else listOf(it) }
        .filterIsInstance(klass)
        .firstOrNull()
        ?: error("Unable to find bindings for ${klass.name}")
}

/** Use no-arg extension function instead: [Fragment.bindings] */
fun <T : Any> Fragment.bindings(klass: Class<T>): T {
    // search dagger components in fragment hierarchy, then fallback to activity and application
    return generateSequence(this, Fragment::getParentFragment)
        .filterIsInstance<DaggerComponentOwner>()
        .map { it.daggerComponent }
        .flatMap { if (it is Collection<*>) it else listOf(it) }
        .filterIsInstance(klass)
        .firstOrNull()
        ?: requireActivity().bindings(klass)
}
