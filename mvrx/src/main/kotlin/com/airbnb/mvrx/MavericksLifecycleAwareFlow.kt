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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.selects.SelectBuilder
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.yield

/**
 * Emits values from the source flow only when the owner is started.
 * When the owner transitions to started, the most recent value will be emitted.
 *
 * Implementation is similar to [Flow.combineTransform] with the following changes:
 * 1. Regular channels are used instead of fair channels to avoid unnecessary [yield] calls.
 *    It's possible because lifecycle state updated in the main thread
 * 2. Flow completes when either [this] flow completes or lifecycle is destroyed
 */
fun <T : Any> Flow<T>.flowWhenStarted(owner: LifecycleOwner): Flow<T> = flow {
    coroutineScope {
        val startedChannel = startedChannel(owner.lifecycle)
        val flowChannel = produce { collect { send(it) } }

        val transform: suspend (Boolean, T) -> Unit = { started, value ->
            if (started) {
                emit(value)
            }
        }

        var started: Boolean? = null
        var flowValue: T? = null
        var isClosed = false

        while (!isClosed) {
            select<Unit> {
                onReceive(startedChannel, { flowChannel.cancel(); isClosed = true }) {
                    started = it
                    if (flowValue !== null) {
                        transform(it, flowValue as T)
                    }
                }
                onReceive(flowChannel, { isClosed = true }) {
                    flowValue = it
                    if (started !== null) {
                        transform(started as Boolean, it)
                    }
                }
            }
        }
    }
}

private fun startedChannel(owner: Lifecycle): Channel<Boolean> {
    val channel = Channel<Boolean>(Channel.CONFLATED)
    val observer = object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            channel.offer(true)
        }

        override fun onStop(owner: LifecycleOwner) {
            channel.offer(false)
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

private inline fun <T : Any> SelectBuilder<Unit>.onReceive(
    channel: ReceiveChannel<T>,
    crossinline onClosed: () -> Unit,
    noinline onReceive: suspend (value: T) -> Unit
) {
    @Suppress("DEPRECATION")
    channel.onReceiveOrNull {
        if (it === null) onClosed()
        else onReceive(it)
    }
}
