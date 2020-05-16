@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.airbnb.mvrx

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combineTransform

/**
 * Emits values from the source flow only when the owner is started.
 * When the owner transitions to started, the most recent value will be emitted.
 */
fun <T : Any> Flow<T>.flowWhenStarted(owner: LifecycleOwner): Flow<T> {
    val startedFlow = MutableStateFlow(false)
    owner.lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            startedFlow.value = true
        }

        override fun onStop(owner: LifecycleOwner) {
            startedFlow.value = false
        }
    })
    return combineTransform<T, Boolean, T>(startedFlow) { value, started ->
        if (started) emit(value)
    }
}
