package com.airbnb.mvrx.mocking

import androidx.annotation.VisibleForTesting
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.Success
import java.util.LinkedList
import kotlin.reflect.KProperty0
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

/**
 * When a lambda has a receiver that extends this class it can make use of this special DSL syntax for changing properties on a data class.
 * This syntax makes it easier to change deeply nested properties.
 *
 * Example: mavericksState.set { ::listing { ::host { ::name } } }.with { "Elena" }
 */
interface DataClassSetDsl {
    /**
     * A small utility function to help copy kotlin data classes when changing a single property, especially where there is deep nesting.
     * This is heavy on reflection, so isn't intended for high performance use cases.
     *
     * This can only be called on Kotlin Data classes, and all nested objects that are modified must also be data classes.
     *
     * @sample setExample
     */
    fun <DataClass : Any, Type> DataClass.set(block: DataClass.() -> KProperty0<Type>): Setter<DataClass, Type> {
        return Setter(this, block())
    }

    /**
     * A shortcut to setting a property to null, instead of "data.set { ::property }.with { null }".
     */
    fun <DataClass : Any, Type> DataClass.setNull(block: DataClass.() -> KProperty0<Type?>): DataClass {
        return Setter(this, block()).with { null }
    }

    /** Shortcut to set a Boolean property to true. */
    fun <DataClass : Any> DataClass.setTrue(block: DataClass.() -> KProperty0<Boolean?>): DataClass {
        return Setter(this, block()).with { true }
    }

    /** Shortcut to set a Boolean property to false. */
    fun <DataClass : Any> DataClass.setFalse(block: DataClass.() -> KProperty0<Boolean?>): DataClass {
        return Setter(this, block()).with { false }
    }

    /** Shortcut to set a numeric property to zero. */
    fun <DataClass : Any> DataClass.setZero(block: DataClass.() -> KProperty0<Number?>): DataClass {
        return Setter(this, block()).with { 0 }
    }

    /** Shortcut to set a List property to an empty list. */
    fun <DataClass : Any, T> DataClass.setEmpty(block: DataClass.() -> KProperty0<List<T>?>): DataClass {
        return Setter(this, block()).with { emptyList() }
    }

    /**
     * A shortcut to setting an Async property to Loading, instead of "data.set { ::property }.with { Loading() }".
     */
    fun <DataClass : Any, Type : Async<AsyncType>, AsyncType> DataClass.setLoading(block: DataClass.() -> KProperty0<Type>): DataClass {
        return Setter<DataClass, Async<AsyncType>?>(this, block()).with { Loading() }
    }

    /**
     * A shortcut to setting an Async property to Fail, instead of "data.set { ::property }.with { Fail() }".
     */
    fun <DataClass : Any, Type : Async<AsyncType>, AsyncType> DataClass.setNetworkFailure(block: DataClass.() -> KProperty0<Type>): DataClass {
        return Setter<DataClass, Async<AsyncType>?>(this, block()).with { Fail(Throwable("Network request failed")) }
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <Type1 : Any, Type2> KProperty0<Type1?>.invoke(block: Type1.() -> KProperty0<Type2>): KProperty0<Type2> {
        val thisValue = requireNotNull(get()) { "The value for '$name' is null, properties on it can't be changed." }

        return NestedProperty(
            this as KProperty0<Type1>,
            thisValue.block()
        )
    }

    /**
     * Use this to update the property value of an Async's type when it is in the Success state. This will throw an exception if the Async it is used
     * on is not Success.
     *
     * Example: state.set { ::myAsync { success { ::text } } }.with { "hello" }
     *
     * If you would like to replace the whole value of the Success object you can instead do
     * state.set { ::myAsync }.with { Success(MyClass(text = "hello")) }
     */
    fun <T, Type> Async<T>.success(block: T.() -> KProperty0<Type>): KProperty0<Type> {
        val successValue = this.invoke()
            ?: error("Async value is not in the success state, it is `${this::class.simpleName}`")
        return successValue.block()
    }

    /**
     * Provide the new value to update your property to. "it" in the lambda is the lambda is the current value, and you should return the new value.
     */
    fun <PropertyType, DataClass : Any> Setter<DataClass, PropertyType>.with(block: (PropertyType) -> PropertyType): DataClass {
        return set(block(property.get()))
    }

    private fun setExample() {

        data class DisclaimerInfo(val text: String)
        data class BookingDetails(val num: Int = 7, val disclaimerInfo: DisclaimerInfo?)
        data class State(val bookingDetails: BookingDetails)

        val myState = State(BookingDetails(disclaimerInfo = DisclaimerInfo("text")))
        // Using the helper utils we can update the nested text value like this
        myState.set { ::bookingDetails { ::disclaimerInfo { ::text } } }.with { "hello world" }

        // The standard way to set a nested value is much longer
        myState.copy(
            bookingDetails = myState.bookingDetails.copy(
                disclaimerInfo = myState.bookingDetails.disclaimerInfo?.copy(
                    text = "hello world"
                )
            )
        )
    }

    /**
     * Helper to copy a nested class and update a property on it.
     */
    class Setter<DataClass : Any, PropType>(private val instance: DataClass, internal val property: KProperty0<PropType>) {
        init {
            val clazz = instance::class
            if (instance is Async<*>) {
                require(instance is Success<*>) {
                    "Cannot set ${clazz.simpleName} property ${property.nameForErrorMsg()}, the Async value it is in is not in the Success state"
                }
            } else {
                require(clazz.isData) {
                    "${clazz.simpleName} property '${property.nameForErrorMsg()}' must be a data class to change mock values with 'set'"
                }
            }
            // We could support other class types as long as they have predictable builder methods, like AutoValue classes.
            // We just need to create reflection based copy methods for them
        }

        /**
         * This assumes the data class is the top level data class.
         * This doesn't work to start in the middle of the chain.
         */
        @Suppress("UNCHECKED_CAST")
        fun getValue(dataClass: DataClass): PropType {
            val propertyChain = LinkedList<KProperty0<Any?>>()

            var nextProp: KProperty0<Any?>? = property
            while (nextProp != null) {
                propertyChain.add(0, nextProp)
                nextProp = (nextProp as? NestedProperty<*, *>)?.wrapperProperty
            }

            return propertyChain.fold<KProperty0<Any?>, Any?>(dataClass) { data, kProp0 ->
                checkNotNull(data) {
                    "Value of data class is null, cannot get property ${kProp0.name}"
                }

                val kProp1 = data::class.memberProperties.singleOrNull { it.name == kProp0.name }
                    ?: error("Could not find property of name ${kProp0.name} on class ${data::class.simpleName}")

                // it is valid for the final result to be null, but intermediate values in the chain
                // cannot be null.
                kProp1.call(data)
            } as PropType
        }

        @Suppress("UNCHECKED_CAST")
        @VisibleForTesting
        fun set(value: PropType): DataClass {

            val (recursiveProperty: KProperty0<Any?>, recursiveValue: Any?) = when (property) {
                is NestedProperty<*, *> -> property.wrapperProperty to (
                    (property.buildSetter() as Setter<Any, Any?>).set(
                        value
                    )
                    )
                else -> property to value
            }

            return if (instance is Success<*>) {
                val successValue = instance.invoke()
                    ?: error("Success value is null - cannot set ${property.name}")
                require(successValue::class.isData) { "${successValue::class.simpleName} must be a data class" }
                val updatedSuccess = successValue.callCopy(recursiveProperty.name to recursiveValue)
                Success(updatedSuccess) as DataClass
            } else {
                instance.callCopy(recursiveProperty.name to recursiveValue)
            }
        }
    }

    /**
     * Represents two properties that are associated by a nested object hierarchy.
     *
     * @property wrapperProperty A property whose type contains the nestedProperty
     * @property nestedProperty The property that this class represents.
     */
    open class NestedProperty<Type : Any, NestedType>(
        val wrapperProperty: KProperty0<Type>,
        val nestedProperty: KProperty0<NestedType>
    ) : KProperty0<NestedType> by nestedProperty {

        init {
            wrapperProperty.isAccessible = true
            nestedProperty.isAccessible = true
        }

        open fun buildSetter(): Setter<Type, NestedType> {
            return Setter(wrapperProperty.get(), nestedProperty)
        }
    }
}

private fun KProperty0<*>.nameForErrorMsg(): String {
    return when (this) {
        is DataClassSetDsl.NestedProperty<*, *> -> "${wrapperProperty.nameForErrorMsg()}:${nestedProperty.nameForErrorMsg()}"
        else -> name
    }
}
