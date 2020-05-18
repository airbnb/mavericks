@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.airbnb.mvrx

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.onCompletion

/**
 * Emits values from the source flow only when the owner is started.
 * When the owner transitions to started, the most recent value will be emitted.
 */
fun <T : Any> Flow<T>.flowWhenStarted(owner: LifecycleOwner): Flow<T> {
    val startedChannel = startedChannel(owner.lifecycle)
    return onCompletion {
        startedChannel.cancel()
    }.combineTransform<T, Boolean, T>(startedChannel.consumeAsFlow()) { value, started ->
        if (started) emit(value)
    }
}

private fun startedChannel(owner: Lifecycle): Channel<Boolean> {
    val channel = Channel<Boolean>(Channel.BUFFERED)
    val observer = object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            channel.offer(true)
        }

        override fun onStop(owner: LifecycleOwner) {
            channel.offer(false)
        }

        override fun onDestroy(owner: LifecycleOwner) {
            channel.cancel()
        }
    }
    owner.addObserver(observer)
    channel.invokeOnClose {
        owner.removeObserver(observer)
    }
    return channel
}