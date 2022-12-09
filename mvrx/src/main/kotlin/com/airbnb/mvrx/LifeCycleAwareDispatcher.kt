package com.airbnb.mvrx

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext

/**
 * Creates coroutine dispatcher that observes provided [Lifecycle] and reacts accordingly:
 * - pauses continuation when state is less than [Lifecycle.State.STARTED]
 * - resumes continuation when state is [Lifecycle.State.STARTED]
 * - cancels continuation when state is [Lifecycle.State.DESTROYED]
 */
internal suspend fun Lifecycle.lifeCycleAwareDispatcher(): CoroutineDispatcher {
    return withContext(Dispatchers.Main.immediate) {
        val parentJob = requireNotNull(coroutineContext[Job]) { "missing parent job" }
        val pausingDispatcher = PausingDispatcher()
        val lifecycleObserver = object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when {
                    source.lifecycle.currentState == Lifecycle.State.DESTROYED -> {
                        removeObserver(this)
                        pausingDispatcher.stop()
                        parentJob.cancel()
                    }
                    source.lifecycle.currentState < Lifecycle.State.STARTED -> pausingDispatcher.pause()
                    else -> pausingDispatcher.resume()
                }
            }
        }
        addObserver(lifecycleObserver)
        pausingDispatcher
    }
}