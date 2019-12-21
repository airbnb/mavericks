@file:SuppressWarnings("Detekt.StringLiteralDuplication")

package com.airbnb.mvrx

import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import kotlinx.metadata.Flag
import kotlinx.metadata.jvm.KotlinClassHeader
import kotlinx.metadata.jvm.KotlinClassMetadata
import java.io.Serializable
import java.lang.reflect.Method

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

private val defaultMarker by lazy {
    try {
        Class.forName("kotlin.jvm.internal.DefaultConstructorMarker")
    } catch (e: ClassNotFoundException) {
        null
    }
}

/**
 * Iterates through all member properties annotated with [PersistState] and parcels them into a bundle that can be
 * saved with savedInstanceState.
 */
internal fun <T : Any> T.persistState(assertCollectionPersistability: Boolean = false): Bundle {
    defaultMarker ?: return Bundle()
    val jvmClass = this::class.java
    val kmClass = (KotlinClassMetadata.read(jvmClass.readMetadata()) as KotlinClassMetadata.Class).toKmClass()
    val constructor = jvmClass.constructors.firstOrNull { it.parameters.any { it.isAnnotationPresent(PersistState::class.java) } } ?: return Bundle()
    val defaultConstructor = jvmClass.constructors.firstOrNull { it.parameters.any { it.type == defaultMarker } } ?: return Bundle()
    val kmConstructor = kmClass.constructors.first { Flag.IS_FINAL(it.flags) }

    if (kmConstructor.valueParameters.size != constructor.parameterCount) {
        if (assertCollectionPersistability) error("Java constructor and kotlin constructor have different parameter lengths!")
        return Bundle()
    }

    val bundle = Bundle()
    constructor.parameters.forEachIndexed { i, p ->
        if (!p.isAnnotationPresent(PersistState::class.java)) return@forEachIndexed

        if (assertCollectionPersistability && p.type != defaultConstructor.parameters[i].type) {
            error("Parameter $i as a different type between the annotated constructor and synthetic default constructor.")
        }
        val fieldName = kmConstructor.valueParameters[i].name
        val field = jvmClass.getDeclaredField(fieldName)
        field.isAccessible = true
        val value = field.get(this)
        if (assertCollectionPersistability) assertCollectionPersistability(value)
        bundle.putAny(i.toString(), value)
    }
    return bundle
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
    if (item !is Serializable && item !is Parcelable) throw IllegalStateException("Cannot parcel ${item::class.java.simpleName}")
}

private fun <T : Any?> Bundle.putAny(key: String?, value: T): Bundle {
    when (value) {
        is Parcelable -> putParcelable(key, value)
        is Serializable -> putSerializable(key, value)
        null -> putString(key, null)
        else -> error("Cannot persist $key. It must be null, Serializable, or Parcelable.")
    }
    return this
}

/**
 * Updates the initial state object given state persisted with [PersistState] in a [Bundle].
 */
internal fun <T : MvRxState> Bundle.restorePersistedState(initialState: T): T {
    defaultMarker ?: return initialState
    val jvmClass = initialState::class.java
    // If we don't set the correct class loader, when the bundle is restored in a new process, it will have the system class loader which
    // can't unmarshal any custom classes.
    classLoader = jvmClass.classLoader

    val constructor = jvmClass.constructors.firstOrNull { it.parameters.any { it.isAnnotationPresent(PersistState::class.java) } } ?: return initialState
    val copyFunction = jvmClass.declaredMethods.first { it.name == "copy\$default" }
    val fieldCount = constructor.parameterCount

    val parameterBitMasks = IntArray(fieldCount / 32 + 1) { 0 }
    val parameters = arrayOfNulls<Any?>(fieldCount)
    parameters[0] = initialState
    for (i in 0..(fieldCount - 1)) {
        val bundleKey = i.toString()
        if (containsKey(bundleKey)) {
            parameters[i] = get(bundleKey)
            continue
        }

        val parameter = copyFunction.parameters[i + 1]
        parameterBitMasks[i / 32] = parameterBitMasks[i / 32] or (1 shl (i % 32))
        parameters[i] = when (parameter.type) {
            Integer.TYPE -> 0
            java.lang.Boolean.TYPE -> false
            java.lang.Float.TYPE -> 0f
            Character.TYPE -> 'A'
            java.lang.Byte.TYPE -> Byte.MIN_VALUE
            java.lang.Short.TYPE -> Short.MIN_VALUE
            Integer.TYPE -> 0
            java.lang.Long.TYPE -> 0L
            java.lang.Double.TYPE -> 0.0
            else -> null
        }
    }

    @Suppress("UNCHECKED_CAST")
    val newState = copyFunction.invoke(null, *arrayOf(initialState, *parameters, *parameterBitMasks.toTypedArray(), null)) as T

    return newState
}

private fun <T, R> Array<T>.firstNotEmptyOrNull(mapper: (T) -> List<R>?): List<R>? {
    forEach { v ->
        val listOrNull = mapper(v)
        if (listOrNull?.isNotEmpty() == true) return listOrNull
    }
    return null
}

/**
 * For some reason, Buck doesn't allow you to call internal methods from the same package in test/
 * However, these methods shouldn't populate the global namespace so this helper is a hack around Buck.
 */
@VisibleForTesting
object PersistStateTestHelpers {
    fun <T : MvRxState> persistState(state: T) = state.persistState(assertCollectionPersistability = true)
    fun <T : MvRxState> restorePersistedState(bundle: Bundle, initialState: T) = bundle.restorePersistedState(initialState)
}

/**
 * Throws [NoSuchElementException] if there is no method.
 */
@Suppress("UNCHECKED_CAST")
private fun <T : Any> Class<T>.copyMethod(): Method = this.declaredMethods.first { it.name == "copy\$default" }

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
private fun Class<*>.readMetadata(): KotlinClassHeader {
    return getAnnotation(Metadata::class.java).run {
        KotlinClassHeader(kind, metadataVersion, bytecodeVersion, data1, data2, extraString, packageName, extraInt)
    }
}