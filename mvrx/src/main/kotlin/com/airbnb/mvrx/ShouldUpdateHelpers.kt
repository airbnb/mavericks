package com.airbnb.mvrx

import kotlin.reflect.KProperty1

fun <S : Any> propertyWhitelist(vararg whitelist: KProperty1<S, *>): (S, S) -> Boolean = { oldState, newState ->
    whitelist.isEmpty() || whitelist.any { it.call(oldState) != it.call(newState) }
}

fun <S : Any, T : Async<*>> onSuccess(property: KProperty1<S, T>): (S, S) -> Boolean = { oldState, newState ->
    property.get(oldState) !is Success<*> && property.get(newState) is Success<*>
}