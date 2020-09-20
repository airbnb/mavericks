package com.airbnb.mvrx

import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlin.reflect.KProperty1

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun <VM : MavericksViewModel<S>, S : MavericksState, A> VM.onEach1Internal(
    owner: LifecycleOwner?,
    prop1: KProperty1<S, A>,
    deliveryMode: DeliveryMode = RedeliverOnStart,
    action: suspend (A) -> Unit
) = stateFlow
    .map { MavericksTuple1(prop1.get(it)) }
    .distinctUntilChanged()
    .resolveSubscription(owner, deliveryMode.appendPropertiesToId(prop1)) { (a) ->
        action(a)
    }

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun <VM : MavericksViewModel<S>, S : MavericksState> VM.onEachInternal(
    owner: LifecycleOwner?,
    deliveryMode: DeliveryMode = RedeliverOnStart,
    action: suspend (S) -> Unit
) = stateFlow.resolveSubscription(owner, deliveryMode, action)

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun <VM : MavericksViewModel<S>, S : MavericksState, A, B> VM.onEach2Internal(
    owner: LifecycleOwner?,
    prop1: KProperty1<S, A>,
    prop2: KProperty1<S, B>,
    deliveryMode: DeliveryMode = RedeliverOnStart,
    action: suspend (A, B) -> Unit
) = stateFlow
    .map { MavericksTuple2(prop1.get(it), prop2.get(it)) }
    .distinctUntilChanged()
    .resolveSubscription(owner, deliveryMode.appendPropertiesToId(prop1, prop2)) { (a, b) ->
        action(a, b)
    }

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun <VM : MavericksViewModel<S>, S : MavericksState, A, B, C> VM.onEach3Internal(
    owner: LifecycleOwner?,
    prop1: KProperty1<S, A>,
    prop2: KProperty1<S, B>,
    prop3: KProperty1<S, C>,
    deliveryMode: DeliveryMode = RedeliverOnStart,
    action: suspend (A, B, C) -> Unit
) = stateFlow
    .map { MavericksTuple3(prop1.get(it), prop2.get(it), prop3.get(it)) }
    .distinctUntilChanged()
    .resolveSubscription(owner, deliveryMode.appendPropertiesToId(prop1, prop2, prop3)) { (a, b, c) ->
        action(a, b, c)
    }

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun <VM : MavericksViewModel<S>, S : MavericksState, A, B, C, D> VM.onEach4Internal(
    owner: LifecycleOwner?,
    prop1: KProperty1<S, A>,
    prop2: KProperty1<S, B>,
    prop3: KProperty1<S, C>,
    prop4: KProperty1<S, D>,
    deliveryMode: DeliveryMode = RedeliverOnStart,
    action: suspend (A, B, C, D) -> Unit
) = stateFlow
    .map { MavericksTuple4(prop1.get(it), prop2.get(it), prop3.get(it), prop4.get(it)) }
    .distinctUntilChanged()
    .resolveSubscription(owner, deliveryMode.appendPropertiesToId(prop1, prop2, prop3, prop4)) { (a, b, c, d) ->
        action(a, b, c, d)
    }

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun <VM : MavericksViewModel<S>, S : MavericksState, A, B, C, D, E> VM.onEach5Internal(
    owner: LifecycleOwner?,
    prop1: KProperty1<S, A>,
    prop2: KProperty1<S, B>,
    prop3: KProperty1<S, C>,
    prop4: KProperty1<S, D>,
    prop5: KProperty1<S, E>,
    deliveryMode: DeliveryMode = RedeliverOnStart,
    action: suspend (A, B, C, D, E) -> Unit
) = stateFlow
    .map { MavericksTuple5(prop1.get(it), prop2.get(it), prop3.get(it), prop4.get(it), prop5.get(it)) }
    .distinctUntilChanged()
    .resolveSubscription(owner, deliveryMode.appendPropertiesToId(prop1, prop2, prop3, prop4, prop5)) { (a, b, c, d, e) ->
        action(a, b, c, d, e)
    }

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun <VM : MavericksViewModel<S>, S : MavericksState, A, B, C, D, E, F> VM.onEach6Internal(
    owner: LifecycleOwner?,
    prop1: KProperty1<S, A>,
    prop2: KProperty1<S, B>,
    prop3: KProperty1<S, C>,
    prop4: KProperty1<S, D>,
    prop5: KProperty1<S, E>,
    prop6: KProperty1<S, F>,
    deliveryMode: DeliveryMode = RedeliverOnStart,
    action: suspend (A, B, C, D, E, F) -> Unit
) = stateFlow
    .map { MavericksTuple6(prop1.get(it), prop2.get(it), prop3.get(it), prop4.get(it), prop5.get(it), prop6.get(it)) }
    .distinctUntilChanged()
    .resolveSubscription(owner, deliveryMode.appendPropertiesToId(prop1, prop2, prop3, prop4, prop5, prop6)) { (a, b, c, d, e, f) ->
        action(a, b, c, d, e, f)
    }

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun <VM : MavericksViewModel<S>, S : MavericksState, A, B, C, D, E, F, G> VM.onEach7Internal(
    owner: LifecycleOwner?,
    prop1: KProperty1<S, A>,
    prop2: KProperty1<S, B>,
    prop3: KProperty1<S, C>,
    prop4: KProperty1<S, D>,
    prop5: KProperty1<S, E>,
    prop6: KProperty1<S, F>,
    prop7: KProperty1<S, G>,
    deliveryMode: DeliveryMode = RedeliverOnStart,
    action: suspend (A, B, C, D, E, F, G) -> Unit
) = stateFlow
    .map { MavericksTuple7(prop1.get(it), prop2.get(it), prop3.get(it), prop4.get(it), prop5.get(it), prop6.get(it), prop7.get(it)) }
    .distinctUntilChanged()
    .resolveSubscription(owner, deliveryMode.appendPropertiesToId(prop1, prop2, prop3, prop4, prop5, prop6, prop7)) { (a, b, c, d, e, f, g) ->
        action(a, b, c, d, e, f, g)
    }

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal fun <VM : MavericksViewModel<S>, S : MavericksState, T> VM.onAsyncInternal(
    owner: LifecycleOwner?,
    asyncProp: KProperty1<S, Async<T>>,
    deliveryMode: DeliveryMode = RedeliverOnStart,
    onFail: (suspend (Throwable) -> Unit)? = null,
    onSuccess: (suspend (T) -> Unit)? = null
) = onEach1Internal(owner, asyncProp, deliveryMode.appendPropertiesToId(asyncProp)) { asyncValue ->
    if (onSuccess != null && asyncValue is Success) {
        onSuccess(asyncValue())
    } else if (onFail != null && asyncValue is Fail) {
        onFail(asyncValue.error)
    }
}