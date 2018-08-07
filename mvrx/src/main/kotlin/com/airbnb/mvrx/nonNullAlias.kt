package com.airbnb.mvrx

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

@Suppress("ClassName")
fun <T : Any> nonNullAlias(getter: () -> T?) = object : ReadOnlyProperty<Any?, T> {
    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T =
            getter() ?: throw NullPointerException("Value for ${property.name} should not be null.")
}