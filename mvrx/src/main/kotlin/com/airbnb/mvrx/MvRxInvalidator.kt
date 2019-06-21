package com.airbnb.mvrx

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import androidx.lifecycle.Lifecycle

internal object MvRxInvalidator {
    // Set of MvRxView or StatefulViews identity hash codes that have a pending invalidate.
    private val pendingInvalidates = HashSet<Int>()
    private val handler = Handler(Looper.getMainLooper(), Handler.Callback { message ->
        val obj = message.obj
        pendingInvalidates.remove(System.identityHashCode(obj))
        when {
            obj is MvRxView -> if (obj.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) obj.invalidate()
            obj is StatefulView && obj is View -> if (obj.isAttachedToWindow()) obj.onInvalidate()
        }
        true
    })

    fun post(obj: Any) {
        if (pendingInvalidates.add(System.identityHashCode(obj))) {
            handler.sendMessage(Message.obtain(handler, System.identityHashCode(obj), obj))
        }
    }
}