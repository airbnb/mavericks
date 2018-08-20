package com.airbnb.mvrx

import kotlin.reflect.KProperty1

/**
 * Subscribes to changes in state properties.
 *
 * @param skipFirst determines whether or not the subscriber will be called with the initial state.
 */
fun <S : Any> propertyWhitelist(
        prop1: KProperty1<S, *>,
        skipFirst: Boolean = false
): (S?, S
) -> Boolean = { oldState, newState ->
    if (oldState == null && skipFirst) false
    else if (oldState == null) true
    else prop1.call(oldState) != prop1.call(newState)
}

/**
 * Subscribes to changes in state properties.
 *
 * @param skipFirst determines whether or not the subscriber will be called with the initial state.
 */
fun <S : Any> propertyWhitelist(
        prop1: KProperty1<S, *>,
        prop2: KProperty1<S, *>,
        skipFirst: Boolean = false
): (S?, S) -> Boolean = { oldState, newState ->
    if (oldState == null && skipFirst) false
    else if (oldState == null) true
    else prop1.call(oldState) != prop1.call(newState) ||
            prop2.call(oldState) != prop2.call(newState)
}


/**
 * Subscribes to changes in state properties.
 *
 * @param skipFirst determines whether or not the subscriber will be called with the initial state
 *                     if it is already [Success]
 */
fun <S : Any> propertyWhitelist(
        prop1: KProperty1<S, *>,
        prop2: KProperty1<S, *>,
        prop3: KProperty1<S, *>,
        skipFirst: Boolean = false
): (S?, S) -> Boolean = { oldState, newState ->
    if (oldState == null && skipFirst) false
    else if (oldState == null) true
    else prop1.call(oldState) != prop1.call(newState) ||
            prop2.call(oldState) != prop2.call(newState) ||
            prop3.call(oldState) != prop3.call(newState)
}


/**
 * Subscribe to an [Async] state property becoming [Success].
 *
 * @param skipFirst determines whether or not the subscriber will be called with the initial state
 *                     if it is already [Success].
 */
fun <S : Any, T : Async<*>> onSuccess(property: KProperty1<S, T>, skipFirst: Boolean = false): (S?, S) -> Boolean = { oldState, newState ->
    if (oldState == null && skipFirst) false
    else if (oldState == null) property.get(newState) is Success<*>
    else property.get(oldState) !is Success<*> && property.get(newState) is Success<*>
}

/**
 * Subscribe to an [Async] state property becoming [Fail].
 *
 * @param skipFirst determines whether or not the subscriber will be called with the initial state
 *                     if it is already [Success].
 */
fun <S : Any, T : Async<*>> onFail(property: KProperty1<S, T>, skipFirst: Boolean = false): (S?, S) -> Boolean = { oldState, newState ->
    if (oldState == null && skipFirst) false
    else if (oldState == null) property.get(newState) is Fail<*>
    else property.get(oldState) !is Fail<*> && property.get(newState) is Fail<*>
}