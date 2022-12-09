/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.airbnb.mvrx

import android.annotation.SuppressLint
import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.whenStateAtLeast
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.util.ArrayDeque
import java.util.Queue
import kotlin.coroutines.CoroutineContext

/**
 * A [CoroutineDispatcher] implementation that maintains a dispatch queue to be able to pause
 * execution of coroutines.
 *
 * @see [DispatchQueue] and [Lifecycle.whenStateAtLeast] for details.
 */
internal class PausingDispatcher : CoroutineDispatcher() {
    private val dispatchQueue = DispatchQueue()

    fun pause() {
        dispatchQueue.pause()
    }

    fun resume() {
        dispatchQueue.resume()
    }

    fun stop() {
        dispatchQueue.finish()
    }

    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        if (Dispatchers.Main.immediate.isDispatchNeeded(context)) {
            return true
        }
        // It's safe to call dispatchQueue.canRun() here because
        // Dispatchers.Main.immediate.isDispatchNeeded returns true if we're not on the main thread
        // If the queue is paused right now we need to dispatch so that the block is added to the
        // the queue
        return !dispatchQueue.canRun()
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        dispatchQueue.dispatchAndEnqueue(context, block)
    }
}

/**
 * Helper class for [PausingDispatcher] that tracks runnables which are enqueued to the dispatcher
 * and also calls back the [PausingDispatcher] when the runnable should run.
 */
internal class DispatchQueue {
    // handler thread
    private var paused: Boolean = true

    // handler thread
    private var finished: Boolean = false
    private var isDraining: Boolean = false

    private val queue: Queue<Runnable> = ArrayDeque()

    @MainThread
    fun pause() {
        paused = true
    }

    @MainThread
    fun resume() {
        if (!paused) {
            return
        }
        check(!finished) {
            "Cannot resume a finished dispatcher"
        }
        paused = false
        drainQueue()
    }

    @MainThread
    fun finish() {
        finished = true
        drainQueue()
    }

    @MainThread
    fun drainQueue() {
        if (isDraining) {
            // Block re-entrant calls to avoid deep stacks
            return
        }
        try {
            isDraining = true
            while (queue.isNotEmpty()) {
                if (!canRun()) {
                    break
                }
                queue.poll()?.run()
            }
        } finally {
            isDraining = false
        }
    }

    @MainThread
    fun canRun() = finished || !paused

    @AnyThread
    @SuppressLint("WrongThread") // false negative, we are checking the thread
    @Suppress("RedundantSamConstructor")
    fun dispatchAndEnqueue(context: CoroutineContext, runnable: Runnable) {
        with(Dispatchers.Main.immediate) {
            // This check is here to handle a special but important case. If for example
            // launchWhenCreated is used while not created it's expected that it will run
            // synchronously when the lifecycle is created. If we called `dispatch` here
            // it launches made before the required state is reached would always be deferred
            // which is not the intended behavior.
            //
            // This means that calling `yield()` while paused and then receiving `resume` right
            // after leads to the runnable being run immediately but that is indeed intended.
            // This could be solved by implementing `dispatchYield` in the dispatcher but it's
            // marked as internal API.
            if (isDispatchNeeded(context) || canRun()) {
                dispatch(context, Runnable { enqueue(runnable) })
            } else {
                enqueue(runnable)
            }
        }
    }

    @MainThread
    private fun enqueue(runnable: Runnable) {
        check(queue.offer(runnable)) {
            "cannot enqueue any more runnables"
        }
        drainQueue()
    }
}