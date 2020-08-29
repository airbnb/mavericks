package com.airbnb.mvrx

import android.os.Build
import android.util.SparseArray
import androidx.collection.ArrayMap
import androidx.collection.LongSparseArray
import androidx.collection.SparseArrayCompat
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KCallable
import kotlin.reflect.KClass

private const val IMMUTABLE_LIST_MESSAGE =
    "Use the immutable listOf(...) method instead. You can append it with `val newList = listA + listB`"
private const val IMMUTABLE_MAP_MESSAGE =
    "Use the immutable mapOf(...) method instead. You can append it with `val newMap = mapA + mapB`"

/**
 * Ensures that the state class is immutable.
 * NOTE: Kotlin collections immutability is a compile-time check only and the underlying classes are
 * mutable so it is impossible to detect them here.
 * Kotlin mutability: https://stackoverflow.com/a/33732403/715633
 *
 * As a result, you may not use MutableList, mutableListOf(...) or the map variants by convention only.
 */
internal fun KClass<*>.assertImmutability() {
    require(java.isData) { "MvRx state must be a data class! - ${this::class.simpleName}" }

    fun Field.isSubtype(vararg classes: KClass<*>): Boolean {
        return classes.any { klass ->
            return when (val returnType = this.type) {
                is ParameterizedType -> klass.java.isAssignableFrom(returnType.rawType as Class<*>)
                else -> klass.java.isAssignableFrom(returnType)
            }
        }
    }

    java.declaredFields
        // During tests, jacoco can add a transient field called jacocoData.
        .filterNot { Modifier.isTransient(it.modifiers) }
        .forEach { prop ->
            when {
                !Modifier.isFinal(prop.modifiers) -> "State property ${prop.name} must be a val, not a var."
                prop.isSubtype(ArrayList::class) -> "You cannot use ArrayList for ${prop.name}.\n$IMMUTABLE_LIST_MESSAGE"
                prop.isSubtype(SparseArray::class) -> "You cannot use SparseArray for ${prop.name}.\n$IMMUTABLE_LIST_MESSAGE"
                prop.isSubtype(LongSparseArray::class) -> "You cannot use LongSparseArray for ${prop.name}.\n$IMMUTABLE_LIST_MESSAGE"
                prop.isSubtype(SparseArrayCompat::class) -> "You cannot use SparseArrayCompat for ${prop.name}.\n$IMMUTABLE_LIST_MESSAGE"
                prop.isSubtype(ArrayMap::class) -> "You cannot use ArrayMap for ${prop.name}.\n$IMMUTABLE_MAP_MESSAGE"
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                    prop.isSubtype(android.util.ArrayMap::class) -> "You cannot use ArrayMap for ${prop.name}.\n$IMMUTABLE_MAP_MESSAGE"
                prop.isSubtype(HashMap::class) -> "You cannot use HashMap for ${prop.name}.\n$IMMUTABLE_MAP_MESSAGE"
                prop.isSubtype(Function::class, KCallable::class) -> {
                    "You cannot use functions inside MvRx state. Only pure data should be represented: ${prop.name}"
                }
                else -> null
            }?.let { throw IllegalArgumentException("Invalid property in state ${this@assertImmutability::class.simpleName}: $it") }
        }
}

/**
 * Since we can only use java reflection, this basically duck types a data class.
 * componentN methods are also used for @PersistState.
 */
internal val Class<*>.isData: Boolean
    get() {
        if (!declaredMethods.any { it.name == "copy\$default" && it.isSynthetic }) {
            return false
        }

        // if the data class property is internal then kotlin appends '$module_name_debug' to the
        // expected function name.
        declaredMethods.firstOrNull { it.name.startsWith("component1") } ?: return false

        declaredMethods.firstOrNull { it.name == "equals" } ?: return false
        declaredMethods.firstOrNull { it.name == "hashCode" } ?: return false
        return true
    }

/**
 * Checks that a state's value is not changed over its lifetime.
 */
internal class MutableStateChecker<S : MavericksState>(val initialState: S) {

    data class StateWrapper<S : MavericksState>(val state: S) {
        private val originalHashCode = hashCode()

        fun validate() = require(originalHashCode == hashCode()) {
            "${state::class.java.simpleName} was mutated. State classes should be immutable."
        }
    }

    private var previousState = StateWrapper(initialState)

    /**
     * Should be called whenever state changes. This validates that the hashcode of each state
     * instance does not change between when it is first set and when the next state is set.
     * If it does change it means different state instances share some mutable data structure.
     */
    fun onStateChanged(newState: S) {
        previousState.validate()
        previousState = StateWrapper(newState)
    }
}
