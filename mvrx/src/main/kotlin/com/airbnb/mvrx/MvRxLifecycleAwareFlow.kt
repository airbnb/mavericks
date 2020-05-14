@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.airbnb.mvrx

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combineTransform

/**
 * Emits values from the source flow only when the owner is started.
 * When the owner transitions to started, the most recent value will be emitted.
 */
fun <T : Any> Flow<T>.flowWhenStarted(owner: LifecycleOwner): Flow<T> {
    val startedFlow = MutableStateFlow(false)
    owner.lifecycle.addObserver(object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun onStart() {
            startedFlow.value = true
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onStop() {
            startedFlow.value = false
        }
    })
    return combineTransform<T, Boolean, T>(startedFlow) { value, started ->
        if (started) emit(value)
    }
}
