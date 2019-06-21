package com.airbnb.mvrx

import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModelStore
import java.lang.ref.ReferenceQueue
import java.lang.ref.WeakReference
import java.util.Timer
import kotlin.concurrent.fixedRateTimer

internal class ViewViewModelStore {

    class ViewReference(
        val view: View,
        queue: ReferenceQueue<View>,
        private val store: ViewModelStore
    ) : WeakReference<View>(view, queue) {

        fun cleanUp() {
            store.clear()
        }
    }

    private lateinit var activity: AppCompatActivity

    private val referenceQueue = ReferenceQueue<View>()
    private val viewMap = mutableMapOf<ViewReference, ViewModelStore>()
    /** Helper to find the right key for [viewMap]. */
    private val viewIdeneityRefMap = mutableMapOf<Int, ViewReference>()

    private var timer: Timer? = null

    fun getStore(view: View): ViewModelStore {
        if (!this::activity.isInitialized) {
            activity = view.context as AppCompatActivity
        }

        val identity = System.identityHashCode(view)
        viewIdeneityRefMap[identity]?.let { ref ->
            return viewMap[ref]!!
        }

        val store = ViewModelStore()
        val ref = ViewReference(view, referenceQueue, store)
        viewIdeneityRefMap[identity] = ref
        viewMap[ref] = store
        ensureTimer()
        return store
    }

    private fun registerLifecycle() {
        activity.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                stopTimer()
                viewMap.values.forEach { it.clear() }
                viewMap.clear()
                viewIdeneityRefMap.clear()
            }
        })
    }

    private fun ensureTimer() {
        if (timer != null) {
            return
        }
        fixedRateTimer(
            name = "expunge_dereferenced_views",
            initialDelay = 60_000,
            period = 60_000
        ) {
            var ref = referenceQueue.poll() as ViewReference?
            while (ref != null) {
                Log.d("Gabe", "Pruning: $ref")
                viewMap.remove(ref)
                viewIdeneityRefMap.remove(System.identityHashCode(ref.view))
                ref.cleanUp()
                ref = referenceQueue.poll() as ViewReference?
            }
            if (viewMap.isEmpty()) {
                stopTimer()
            }
        }
    }

    private fun stopTimer() {
        timer?.cancel()
        timer = null
    }
}