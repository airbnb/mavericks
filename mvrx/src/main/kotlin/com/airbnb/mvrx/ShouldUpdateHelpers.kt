package com.airbnb.mvrx

import kotlin.reflect.KProperty1

fun <S : Any> propertyWhitelist(vararg whitelist: KProperty1<S, *>): (S?, S) -> Boolean = { oldState, newState ->
    if (oldState == null) true
    else whitelist.isEmpty() || whitelist.any { it.call(oldState) != it.call(newState) }
}

fun <S : Any> propertyWhitelistNoInitial(vararg whitelist: KProperty1<S, *>): (S?, S) -> Boolean = { oldState, newState ->
    if (oldState == null) false
    else whitelist.isEmpty() || whitelist.any { it.call(oldState) != it.call(newState) }
}

/**
 * initialValue indicates whether the subscriber should be called if the initial state is Success.
 */
fun <S : Any, T : Async<*>> onSuccess(property: KProperty1<S, T>): (S?, S) -> Boolean = { oldState, newState ->
    if (oldState == null) property.get(newState) is Success<*>
    else property.get(oldState) !is Success<*> && property.get(newState) is Success<*>
}

fun <S : Any, T : Async<*>> onSuccessNoInitial(property: KProperty1<S, T>): (S?, S) -> Boolean = { oldState, newState ->
    if (oldState == null) false
        else onSuccess(property)(oldState, newState)
}

fun <S : Any, T : Async<*>> onFail(property: KProperty1<S, T>): (S?, S) -> Boolean = { oldState, newState ->
    if (oldState == null) property.get(newState) is Fail<*>
    else property.get(oldState) !is Fail<*> && property.get(newState) is Fail<*>
}

fun <S : Any, T : Async<*>> onFailNoInitial(property: KProperty1<S, T>): (S?, S) -> Boolean = { oldState, newState ->
    if (oldState == null) false
        else onFail(property)(oldState, newState)
}