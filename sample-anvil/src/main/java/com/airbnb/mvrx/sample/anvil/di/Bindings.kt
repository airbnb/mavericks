package com.airbnb.mvrx.sample.anvil.di

import android.content.Context
import android.content.ContextWrapper
import androidx.fragment.app.Fragment

/**
 * Use this to get the dagger "Bindings" for your module. Bindings are used if you need to directly interact with a dagger component such as:
 * * an inject function: `inject(MyFragment frag)`
 * * an explicit getter: `fun myClass(): MyClass`
 *
 * Bindings will walk up the Fragment/Activity hierarchy and check for [DaggerComponentOwner] to see if any of its components provide the specified bindings.
 * Most of the time this will "just work" and you don't have to think about it.
 *
 * To inject [@Inject] properties:
 * 1) Add an inject function to YourModuleBindings
 * 2) Make sure your bindings interface is contributed to AppComponent, UserComponent, etc via `@ContributesTo(AppComponent::class)`.
 * 3) Call context.bindings<YourModuleBindings>().inject(this) (Kotlin)
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