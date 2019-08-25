package com.airbnb.mvrx.mock

import kotlin.reflect.KFunction
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.memberFunctions

/** Call the copy function of the Data Class receiver. The params are a map of parameter name to value. */
internal fun <T : Any> T.callCopy(vararg params: Pair<String, Any?>): T {
    val paramMap = params.associate { it }
    val copyFunction = this::class.memberFunctions.filterIsInstance<KFunction<T>>().single { it.name == "copy" }
    return copyFunction.callNamed(paramMap, self = this)
}

internal fun <R> KFunction<R>.callNamed(params: Map<String, Any?>, self: Any? = null, extSelf: Any? = null): R {
    val map = params.mapTo(ArrayList()) { (key, value) ->
        val param = parameters.firstOrNull { it.name == key }
            ?: throw IllegalStateException("No parameter named '$key' found on copy function for '${this.returnType.classifier}'")
        param to value
    }

    if (self != null) map += instanceParameter!! to self
    if (extSelf != null) map += extensionReceiverParameter!! to extSelf
    return callBy(map.toMap())
}