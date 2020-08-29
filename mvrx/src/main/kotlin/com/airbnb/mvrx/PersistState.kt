@file:SuppressWarnings("Detekt.StringLiteralDuplication")

package com.airbnb.mvrx

import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import java.io.Serializable
import java.lang.reflect.Constructor
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

/**
 * Iterates through all member properties annotated with [PersistState] and parcels them into a bundle that can be
 * saved with savedInstanceState.
 */
internal fun <T : MavericksState> T.persistState(validation: Boolean = false): Bundle {
    val jvmClass = this::class.java
    // Find the first constructor that has parameters annotated with @PersistState or return.
    val constructor = jvmClass.primaryConstructor() ?: return Bundle()

    val bundle = Bundle()
    constructor.parameterAnnotations.forEachIndexed { i, p ->
        if (p.none { it is PersistState }) return@forEachIndexed
        // For each parameter in the constructor, there is a componentN function because state is a data class.
        // We can rely on this to be true because the MvRxMutabilityHelpers asserts that the state class is a data class.
        // See MvRxMutabilityHelper Class<*>.isData

        val getter = jvmClass.getComponentNFunction(i)

        val value = getter.invoke(this)
        if (validation) assertCollectionPersistability(value)
        bundle.putAny(i.toString(), value)
    }
    return bundle
}

private fun <T : MavericksState> Class<out T>.primaryConstructor(): Constructor<*>? {
    // Assumes that only the primary constructor has PersistState annotations.
    // TODO - potentially throw if multiple constructors have PersistState as that is not supported.
    return constructors.firstOrNull { constructor ->
        constructor.parameterAnnotations.any { paramAnnotations ->
            paramAnnotations.any { it is PersistState }
        }
    }
}

private fun <T : MavericksState> Class<out T>.getComponentNFunction(componentIndex: Int): Method {
    val functionName = "component${componentIndex + 1}"
    return try {
        getDeclaredMethod(functionName)
    } catch (e: NoSuchMethodException) {
        // if the data class property is internal then kotlin appends '$module_name_debug' to the
        // expected function name.
        declaredMethods.firstOrNull { it.name.startsWith("$functionName\$") }
    }
        ?.also { it.isAccessible = true }
        ?: error("Unable to find function $functionName in ${this@getComponentNFunction::class.simpleName}")
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
    if (item !is Serializable && item !is Parcelable) error("Cannot parcel ${item::class.java.simpleName}")
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
internal fun <T : MavericksState> Bundle.restorePersistedState(
    initialState: T,
    validation: Boolean = false
): T {
    val jvmClass = initialState::class.java
    val constructor = jvmClass.primaryConstructor() ?: return initialState

    // If we don't set the correct class loader, when the bundle is restored in a new process, it will have the system class loader which
    // can't unmarshal any custom classes.
    classLoader = jvmClass.classLoader

    // For data classes, Kotlin generates a static function called copy$default.
    // The first parameter is the object to copy from.
    // The next parameters are all of parameters to copy (it's jvm bytecode/java so there are no optional parameters in the generated method).
    // The next parameter(s) are a bitmask. Each parameter index corresponds to one bit in the int.
    //     If the bitmask is 1 for a given parameter then the new object will have the original object's value and the parameter value will be ignored.
    //     If the bitmask is 0 then it will use the value from the parameter.
    //     There is 1 bitmask for every 32 parameters. If there are 48 parameters, there will be 2 bitmasks. Parameter 33 will be the first bit of the 2nd bitmask.
    // The last parameter is ignored. It can be null.
    val copyFunction = jvmClass.declaredMethods.first { it.name == "copy\$default" }
    val fieldCount = constructor.parameterTypes.size

    // There is 1 bitmask for each block of 32 parameters.
    val parameterBitMasks = IntArray(fieldCount / 32 + 1) { 0 }
    val parameters = arrayOfNulls<Any?>(fieldCount)
    parameters[0] = initialState
    for (i in 0 until fieldCount) {
        val bundleKey = i.toString()
        if (containsKey(bundleKey)) {
            // Copy the persisted value into the parameter array.
            // The bitmask for this element will be 0 so this value will be copied to the new object.
            parameters[i] = get(bundleKey)
            continue
        }

        if (validation && constructor.parameterAnnotations[i].any { it is PersistState }) {
            error("savedInstanceState bundle should have a key for state property at position $i but it was missing.")
        }

        // Set the bitmask for this parameter to 1 so it copies the value from the original object.
        parameterBitMasks[i / 32] = parameterBitMasks[i / 32] or (1 shl (i % 32))
        // These parameters will be ignored. We just need to put in something of the correct type to match the method signature.
        parameters[i] = copyFunction.parameterTypes[i + 1].defaultParameterValue
    }

    // See the comment above for information on the parameters here.
    @Suppress("UNCHECKED_CAST")
    return copyFunction.invoke(
        null,
        initialState,
        *parameters,
        *parameterBitMasks.toTypedArray(),
        null
    ) as T
}

/**
 * For some reason, Buck doesn't allow you to call internal methods from the same package in test/
 * However, these methods shouldn't populate the global namespace so this helper is a hack around Buck.
 */
@VisibleForTesting
object PersistStateTestHelpers {
    fun <T : MavericksState> persistState(state: T) = state.persistState(validation = true)
    fun <T : MavericksState> restorePersistedState(
        bundle: Bundle,
        initialState: T,
        validation: Boolean = false
    ) = bundle.restorePersistedState(initialState, validation)
}

private val Class<*>.defaultParameterValue: Any?
    get() = when (this) {
        Integer.TYPE -> 0
        java.lang.Boolean.TYPE -> false
        java.lang.Float.TYPE -> 0f
        Character.TYPE -> 'A'
        java.lang.Byte.TYPE -> Byte.MIN_VALUE
        java.lang.Short.TYPE -> Short.MIN_VALUE
        java.lang.Long.TYPE -> 0L
        java.lang.Double.TYPE -> 0.0
        else -> null
    }
