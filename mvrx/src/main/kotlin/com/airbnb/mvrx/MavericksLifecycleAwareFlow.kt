@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.airbnb.mvrx

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.selects.SelectBuilder
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.yield

/**
 * Emits values from the source flow only when the owner is started.
 * When the owner transitions to started, the most recent value will be emitted.
 *
 * Implementation is similar to [combineTransform] with the following changes:
 * 1. Regular channels are used instead of fair channels to avoid unnecessary [yield] calls.
 *    It's possible because lifecycle state updated in the main thread
 * 2. Flow completes when either [this] flow completes or lifecycle is destroyed
 */
fun <T : Any?> Flow<T>.flowWhenStarted(owner: LifecycleOwner): Flow<T> = flow {
    coroutineScope {
        val startedChannel = startedChannel(owner.lifecycle)
        val flowChannel = produce { collect { send(it) } }

        @Suppress("LocalVariableName")
        val NULL = Any()
        var started: Boolean? = null
        var flowResult: Any? = NULL
        var isClosed = false

        while (!isClosed) {
            val result = select {
                onReceive(startedChannel, { flowChannel.cancel(); isClosed = true; NULL }) { value ->
                    started = value
                    if (flowResult != NULL && value) {
                        flowResult
                    } else {
                        NULL
                    }
                }
                onReceive(flowChannel, { isClosed = true; NULL }) { value ->
                    flowResult = value
                    if (started == true) {
                        value
                    } else {
                        NULL
                    }
                }
            }
            if (result != NULL) {
                @Suppress("UNCHECKED_CAST")
                emit(result as T)
            }
        }
    }
}

private fun startedChannel(owner: Lifecycle): Channel<Boolean> {
    val channel = Channel<Boolean>(Channel.CONFLATED)
    val observer = object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            channel.trySend(true)
        }

        override fun onStop(owner: LifecycleOwner) {
            channel.trySend(false)
        }

        override fun onDestroy(owner: LifecycleOwner) {
            channel.close()
        }
    }
    owner.addObserver(observer)
    channel.invokeOnClose {
        owner.removeObserver(observer)
    }
    return channel
}

private inline fun <T : Any?, R : Any?> SelectBuilder<R>.onReceive(
    channel: ReceiveChannel<T>,
    crossinline onClosed: () -> R,
    noinline onReceive: suspend (value: T) -> R
) {
    channel.onReceiveCatching {
        val result = it.getOrNull()
        if (result === null) {
            onClosed()
        } else {
            onReceive(result)
        }
    }
}
