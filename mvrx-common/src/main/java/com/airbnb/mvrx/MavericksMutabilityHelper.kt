package com.airbnb.mvrx

import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KCallable
import kotlin.reflect.KClass

private const val IMMUTABLE_LIST_MESSAGE =
    "Use the immutable listOf(...) method instead. You can append it with `val newList = listA + listB`"

/**
 * Ensures that the state class is immutable.
 * NOTE: Kotlin collections immutability is a compile-time check only and the underlying classes are
 * mutable so it is impossible to detect them here.
 * Kotlin mutability: https://stackoverflow.com/a/33732403/715633
 *
 * As a result, you may not use MutableList, mutableListOf(...) or the map variants by convention only.
 */
fun assertMavericksDataClassImmutability(
    kClass: KClass<*>,
    allowFunctions: Boolean = false,
) {
    require(kClass.java.isData) { "Mavericks state must be a data class! - ${kClass.simpleName}" }

    val disallowedFieldCollectionTypes = listOfNotNull(
        ArrayList::class.java,
        HashMap::class.java,
        runCatching { Class.forName("android.util.SparseArray") }.getOrNull(),
        runCatching { Class.forName("androidx.collection.LongSparseArray") }.getOrNull(),
        runCatching { Class.forName("androidx.collection.SparseArrayCompat") }.getOrNull(),
        runCatching { Class.forName("androidx.collection.ArrayMap") }.getOrNull(),
        runCatching { Class.forName("android.util.ArrayMap") }.getOrNull(),
    )

    fun Field.isSubtype(vararg classes: KClass<*>): Boolean {
        return classes.any { klass ->
            return when (val returnType = this.type) {
                is ParameterizedType -> klass.java.isAssignableFrom(returnType.rawType as Class<*>)
                else -> klass.java.isAssignableFrom(returnType)
            }
        }
    }

    kClass.java.declaredFields
        // During tests, jacoco can add a transient field called jacocoData.
        .filterNot { Modifier.isTransient(it.modifiers) }
        .forEach { prop ->
            val disallowedFieldCollectionType = disallowedFieldCollectionTypes.firstOrNull { clazz -> prop.isSubtype(clazz.kotlin) }
            when {
                !Modifier.isFinal(prop.modifiers) -> "State property ${prop.name} must be a val, not a var."
                disallowedFieldCollectionType != null -> {
                    "You cannot use ${disallowedFieldCollectionType.simpleName} for ${prop.name}.\n$IMMUTABLE_LIST_MESSAGE"
                }
                !allowFunctions && prop.isSubtype(Function::class, KCallable::class) -> {
                    "You cannot use functions inside Mavericks state. Only pure data should be represented: ${prop.name}"
                }
                else -> null
            }?.let { throw IllegalArgumentException("Invalid property in state ${kClass.simpleName}: $it") }
        }
}

/**
 * Since we can only use java reflection, this basically duck types a data class.
 * componentN methods are also used for @PersistState.
 */
internal val Class<*>.isData: Boolean
    get() {
        // When a value class is present in the constructor, Kotlin mangles the copy method name
        // to avoid signature clashes (e.g., "copy-KtkBMb8$default" instead of "copy$default").
        // We check for either the exact name "copy$default" or the pattern "copy-*$default".
        val hasCopyDefault = declaredMethods.any { method ->
            method.isSynthetic && method.name.let { name ->
                name == "copy\$default" || (name.startsWith("copy-") && name.endsWith("\$default"))
            }
        }
        if (!hasCopyDefault) return false

        // Similarly, component1 can be mangled when it's a value class type.
        // It can also have module names appended for internal properties.
        // Patterns: "component1", "component1$module", "component1-<hash>"
        val hasComponent1 = declaredMethods.any { method ->
            method.name.let { name ->
                name == "component1" ||
                    name.startsWith("component1\$") ||
                    name.startsWith("component1-")
            }
        }
        if (!hasComponent1) return false

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
