package com.airbnb.mvrx

import android.support.v4.util.ArrayMap
import android.support.v4.util.LongSparseArray
import android.support.v4.util.SparseArrayCompat
import android.util.SparseArray
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType

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
    require(this.isData) { "MvRx state must be a data class!" }

    fun KProperty1<*, *>.isSubtype(klass: KClass<*>) =
        returnType.isSubtypeOf(klass.starProjectedType)

    this.declaredMemberProperties.forEach {
        when {
            it is KMutableProperty<*> -> "State property ${it.name} must be a val, not a var."
            it.isSubtype(ArrayList::class) -> "You cannot use ArrayList for ${it.name}.\n$IMMUTABLE_LIST_MESSAGE"
            it.isSubtype(SparseArray::class) -> "You cannot use SparseArray for ${it.name}.\n$IMMUTABLE_LIST_MESSAGE"
            it.isSubtype(LongSparseArray::class) -> "You cannot use LongSparseArray for ${it.name}.\n$IMMUTABLE_LIST_MESSAGE"
            it.isSubtype(SparseArrayCompat::class) -> "You cannot use SparseArrayCompat for ${it.name}.\n$IMMUTABLE_LIST_MESSAGE"
            it.isSubtype(ArrayMap::class) -> "You cannot use ArrayMap for ${it.name}.\n$IMMUTABLE_MAP_MESSAGE"
            it.isSubtype(android.util.ArrayMap::class) -> "You cannot use ArrayMap for ${it.name}.\n$IMMUTABLE_MAP_MESSAGE"
            it.isSubtype(HashMap::class) -> "You cannot use HashMap for ${it.name}.\n$IMMUTABLE_MAP_MESSAGE"
            else -> null
        }?.let { throw IllegalArgumentException(it) }
    }
}

/**
 * Checks that a state's value is not changed over its lifetime.
 */
internal class MutableStateChecker<S : MvRxState>(val initialState: S) {

    data class StateWrapper<S : MvRxState>(val state: S) {
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


