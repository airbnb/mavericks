package com.airbnb.mvrx.persiststate

import android.os.Bundle
import android.os.Parcelable
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.StatePersistor
import java.io.Serializable
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.primaryConstructor

object StatePersistorImpl : StatePersistor {
    override fun <S : MvRxState> persistState(obj: S, assertCollectionPersistability: Boolean): Bundle {
        val persistStateArgs = obj::class.primaryConstructor
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
        return obj::class.declaredMemberProperties.asSequence()
            .filter { persistStateArgNames.contains(it.name) }
            .map { prop ->
                @Suppress("UNCHECKED_CAST")
                val value = (prop as? KProperty1<S, Any?>)?.get(obj)
                if (assertCollectionPersistability) assertCollectionPersistability(value)
                prop to value
            }
            .fold(Bundle()) { bundle, (param, value) -> bundle.putAny(param.name, value) }
    }

    override fun <S : MvRxState> restorePersistedState(bundle: Bundle, initialState: S): S {
        // If we don't set the correct class loader, when the bundle is restored in a new process, it will have the system class loader which
        // can't unmarshal any custom classes.
        val stateClass = initialState::class
        bundle.classLoader = stateClass.java.classLoader
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
                if (bundle.containsKey(param.name)) map[param] = bundle[param.name]
                map
            }
        // Add the instance to call copy on
        copyArgs[copyMethod.instanceParameter ?: throw IllegalStateException("Copy method not a member of a class. This should never happen.")] =
            initialState
        return copyMethod.callBy(copyArgs)
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

    override fun <S : MvRxState> warmReflectionCache(initialState: S) {
        initialState::class.primaryConstructor?.parameters?.forEach { it.annotations }
        initialState::class.declaredMemberProperties.asSequence()
            .filter { it.visibility == KVisibility.PUBLIC }
            .forEach { prop ->
                @Suppress("UNCHECKED_CAST")
                (prop as? KProperty1<S, Any?>)?.get(initialState)
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

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> KClass<T>.copyMethod(): KFunction<T> = this.memberFunctions.first { it.name == "copy" } as KFunction<T>
}