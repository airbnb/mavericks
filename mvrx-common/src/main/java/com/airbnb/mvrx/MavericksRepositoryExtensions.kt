@file:Suppress("FunctionName")

package com.airbnb.mvrx

import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlin.reflect.KProperty1

@InternalMavericksApi
fun <Repository : MavericksRepository<S>, S : MavericksState> Repository._internal(
    action: suspend (S) -> Unit
) = stateFlow.resolveSubscription(action)

@InternalMavericksApi
fun <Repository : MavericksRepository<S>, S : MavericksState, A> Repository._internal1(
    prop1: KProperty1<S, A>,
    action: suspend (A) -> Unit
) = stateFlow
    .map { MavericksTuple1(prop1.get(it)) }
    .distinctUntilChanged()
    .resolveSubscription() { (a) ->
        action(a)
    }

@InternalMavericksApi
fun <Repository : MavericksRepository<S>, S : MavericksState, A, B> Repository._internal2(
    prop1: KProperty1<S, A>,
    prop2: KProperty1<S, B>,
    action: suspend (A, B) -> Unit
) = stateFlow
    .map { MavericksTuple2(prop1.get(it), prop2.get(it)) }
    .distinctUntilChanged()
    .resolveSubscription() { (a, b) ->
        action(a, b)
    }

@InternalMavericksApi
fun <Repository : MavericksRepository<S>, S : MavericksState, A, B, C> Repository._internal3(
    prop1: KProperty1<S, A>,
    prop2: KProperty1<S, B>,
    prop3: KProperty1<S, C>,
    action: suspend (A, B, C) -> Unit
) = stateFlow
    .map { MavericksTuple3(prop1.get(it), prop2.get(it), prop3.get(it)) }
    .distinctUntilChanged()
    .resolveSubscription() { (a, b, c) ->
        action(a, b, c)
    }

@InternalMavericksApi
fun <Repository : MavericksRepository<S>, S : MavericksState, A, B, C, D> Repository._internal4(
    prop1: KProperty1<S, A>,
    prop2: KProperty1<S, B>,
    prop3: KProperty1<S, C>,
    prop4: KProperty1<S, D>,
    action: suspend (A, B, C, D) -> Unit
) = stateFlow
    .map { MavericksTuple4(prop1.get(it), prop2.get(it), prop3.get(it), prop4.get(it)) }
    .distinctUntilChanged()
    .resolveSubscription() { (a, b, c, d) ->
        action(a, b, c, d)
    }

@InternalMavericksApi
fun <Repository : MavericksRepository<S>, S : MavericksState, A, B, C, D, E> Repository._internal5(
    prop1: KProperty1<S, A>,
    prop2: KProperty1<S, B>,
    prop3: KProperty1<S, C>,
    prop4: KProperty1<S, D>,
    prop5: KProperty1<S, E>,
    action: suspend (A, B, C, D, E) -> Unit
) = stateFlow
    .map { MavericksTuple5(prop1.get(it), prop2.get(it), prop3.get(it), prop4.get(it), prop5.get(it)) }
    .distinctUntilChanged()
    .resolveSubscription() { (a, b, c, d, e) ->
        action(a, b, c, d, e)
    }

@InternalMavericksApi
fun <Repository : MavericksRepository<S>, S : MavericksState, A, B, C, D, E, F> Repository._internal6(
    prop1: KProperty1<S, A>,
    prop2: KProperty1<S, B>,
    prop3: KProperty1<S, C>,
    prop4: KProperty1<S, D>,
    prop5: KProperty1<S, E>,
    prop6: KProperty1<S, F>,
    action: suspend (A, B, C, D, E, F) -> Unit
) = stateFlow
    .map { MavericksTuple6(prop1.get(it), prop2.get(it), prop3.get(it), prop4.get(it), prop5.get(it), prop6.get(it)) }
    .distinctUntilChanged()
    .resolveSubscription() { (a, b, c, d, e, f) ->
        action(a, b, c, d, e, f)
    }

@InternalMavericksApi
fun <Repository : MavericksRepository<S>, S : MavericksState, A, B, C, D, E, F, G> Repository._internal7(
    prop1: KProperty1<S, A>,
    prop2: KProperty1<S, B>,
    prop3: KProperty1<S, C>,
    prop4: KProperty1<S, D>,
    prop5: KProperty1<S, E>,
    prop6: KProperty1<S, F>,
    prop7: KProperty1<S, G>,
    action: suspend (A, B, C, D, E, F, G) -> Unit
) = stateFlow
    .map { MavericksTuple7(prop1.get(it), prop2.get(it), prop3.get(it), prop4.get(it), prop5.get(it), prop6.get(it), prop7.get(it)) }
    .distinctUntilChanged()
    .resolveSubscription() { (a, b, c, d, e, f, g) ->
        action(a, b, c, d, e, f, g)
    }

@InternalMavericksApi
fun <Repository : MavericksRepository<S>, S : MavericksState, T> Repository._internalSF(
    asyncProp: KProperty1<S, Async<T>>,
    onFail: (suspend (Throwable) -> Unit)? = null,
    onSuccess: (suspend (T) -> Unit)? = null
) = _internal1(asyncProp) { asyncValue ->
    if (onSuccess != null && asyncValue is Success) {
        onSuccess(asyncValue())
    } else if (onFail != null && asyncValue is Fail) {
        onFail(asyncValue.error)
    }
}
