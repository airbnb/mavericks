@file:SuppressWarnings("Detekt.StringLiteralDuplication")
package com.airbnb.mvrx

import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.VisibleForTesting
import java.io.Serializable
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.primaryConstructor

/**
 * Annotate a field in your MvRxViewModel state with [PersistState] to have it automatically persisted when Android kills your process
 * to free up memory. MvRx will automatically recreate your ViewModel when the process restarts with these fields saved.
 *
 * You should ONLY SAVE what you need to refetch data, not fetched data itself. For example, for search, save the search filters not the
 * search results.
 *
 * You can also only annotate [Serializable] and [android.os.Parcelable] fields.
 *
 * An example state class could look like:
 * data class State(@PersistState val count: Int = 0)
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class PersistState

/**
 * Iterates through all member properties annotated with [PersistState] and parcels them into a bundle that can be
 * saved with savedInstanceState.
 */
internal fun <T : Any> T.persistState(assertCollectionPersistability: Boolean = false): Bundle {
    val klass = this::class
    val persistStateArgs = klass.primaryConstructor
            ?.parameters
            ?.filter { it.annotations.any { it.annotationClass == PersistState::class } }
    if (persistStateArgs?.isEmpty() != false) {
        return Bundle()
    }

    val persistStateArgNames = persistStateArgs.map { it.name }
    /**
     * Filter out params that don't have an associated @PersistState prop.
     * Map the parameter name to the current value of its associated property
     * Reduce the @PersistState parameters into a bundle mapping the parameter name to the property value.
     */
    return klass.declaredMemberProperties.asSequence()
            .filter { persistStateArgNames.contains(it.name) }
            .map { prop ->
                @Suppress("UNCHECKED_CAST")
                val value = (prop as? KProperty1<T, Any?>)?.get(this@persistState)
                if (assertCollectionPersistability) assertCollectionPersistability(value)
                prop to value
            }
            .fold(Bundle()) { bundle, (param, value) -> bundle.putAny(param.name, value) }
}

private fun assertCollectionPersistability(value: Any?) {
    when (value) {
        is Collection<*> -> {
            value
                .filterNotNull()
                .forEach(::assertPersistable)
        }
        is Map<*, *> -> {
            value
                .mapNotNull { it.value }
                .forEach(::assertPersistable)
        }
    }
}

private fun assertPersistable(item: Any) {
    if (item !is Serializable && item !is Parcelable) throw IllegalStateException("Cannot parcel ${item::class.simpleName}")
}

private fun <T : Any?> Bundle.putAny(key: String?, value: T): Bundle {
    when (value) {
        is Parcelable -> putParcelable(key, value)
        is Serializable -> putSerializable(key, value)
        null -> putString(key, null)
        else -> throw IllegalStateException("Cannot persist $key. It must be null, Serializable, or Parcelable.")
    }
    return this
}

/**
 * Updates the initial state object given state persisted with [PersistState] in a [Bundle].
 */
internal fun <T : Any> Bundle.restorePersistedState(initialState: T): T {
    // If we don't set the correct class loader, when the bundle is restored in a new process, it will have the system class loader which
    // can't unmarshall any custom classes.
    val stateClass = initialState::class
    classLoader = stateClass.java.classLoader
    val constructor = stateClass.primaryConstructor ?: throw IllegalStateException("${stateClass.simpleName} has no primary constructor!")
    val persistedConstructorParamNames = constructor.parameters.asSequence()
        .filter { it.name != null }
        .filter { it.annotations.any { it.annotationClass == PersistState::class } }
        .map { it.name }
        .toSet()
    if (persistedConstructorParamNames.isEmpty()) {
        return initialState
    }

    val copyMethod = stateClass.copyMethod()
    val copyArgs = copyMethod.parameters.asSequence()
        .filter { persistedConstructorParamNames.contains(it.name) }
        .fold(mutableMapOf<KParameter, Any?>()) { map, param ->
            // We do the containsKey check to differentiate between a missing key and one that is explicitly null.
            if (containsKey(param.name)) map[param] = this[param.name]
            map
        }
    // Add the instance to call copy on
    copyArgs[copyMethod.instanceParameter ?: throw IllegalStateException("Copy method not a member of a class. This should never happen.")] =
        initialState
    return copyMethod.callBy(copyArgs)
}

/**
 * For some reason, Buck doesn't allow you to call internal methods from the same package in test/
 * However, these methods shouldn't populate the global namespace so this helper is a hack around Buck.
 */
@VisibleForTesting
object PersistStateTestHelpers {
    fun <T : Any> persistState(state: T) = state.persistState(assertCollectionPersistability = true)
    fun <T : Any> restorePersistedState(bundle: Bundle, initialState: T) = bundle.restorePersistedState(initialState)
}